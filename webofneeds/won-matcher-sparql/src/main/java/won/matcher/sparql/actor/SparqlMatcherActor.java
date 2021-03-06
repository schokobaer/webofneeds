package won.matcher.sparql.actor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelExtract;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StatementBoundary;
import org.apache.jena.rdf.model.StatementBoundaryBase;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.jsonldjava.core.JsonLdError;

import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.actor.UntypedActor;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import won.matcher.service.common.event.BulkHintEvent;
import won.matcher.service.common.event.BulkNeedEvent;
import won.matcher.service.common.event.HintEvent;
import won.matcher.service.common.event.NeedEvent;
import won.matcher.sparql.config.SparqlMatcherConfig;

/**
 * Siren/Solr based abstract matcher with all implementations for querying as
 * well as indexing needs.
 */
@Component
@Scope("prototype")
public class SparqlMatcherActor extends UntypedActor {
	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	
	private ActorRef pubSubMediator;
	
	@Autowired
	private SparqlMatcherConfig config;
	
	@Override
    public void preStart() throws IOException {

        // subscribe to need events
        pubSubMediator = DistributedPubSub.get(getContext().system()).mediator();
    }

	@Override
	public void onReceive(final Object o) throws Exception {

		if (o instanceof NeedEvent) {
			NeedEvent needEvent = (NeedEvent) o;
			if (needEvent.getEventType().equals(NeedEvent.TYPE.ACTIVE)) {
				processActiveNeedEvent(needEvent);
			} else if (needEvent.getEventType().equals(NeedEvent.TYPE.INACTIVE)) {
				processInactiveNeedEvent(needEvent);
			} else {
				unhandled(o);
			}
		} else if (o instanceof BulkNeedEvent) {
			log.info("received bulk need event, processing {} need events ...",
					((BulkNeedEvent) o).getNeedEvents().size());
			for (NeedEvent event : ((BulkNeedEvent) o).getNeedEvents()) {
				processActiveNeedEvent(event);
			}
		} else {
			unhandled(o);
		}
	}

	protected void processInactiveNeedEvent(NeedEvent needEvent) throws IOException, JsonLdError {
		log.info("Received inactive need.");
	}

	private static String hashFunction(Object input) {
		return Integer.toHexString(input.hashCode());
	}
	
	private static final Var resultName = Var.alloc("result");
	
	private static BasicPattern createDetailsQuery(Model model) {
		BasicPattern pattern = new BasicPattern();
		
		StreamSupport.stream(Spliterators.spliteratorUnknownSize(model.listStatements(), Spliterator.CONCURRENT), true)
		.map((statement) -> {
			Triple triple = statement.asTriple();
			RDFNode object = statement.getObject();
			
			Node newSubject = NodeFactory.createVariable(hashFunction(triple.getSubject()));
			
			Node newObject = triple.getObject();
			
			if(object.isAnon()) {
				newObject = NodeFactory.createVariable(hashFunction(newObject));
			}
			
			return new Triple(newSubject, triple.getPredicate(), newObject);
		}).forEach(pattern::add);
		
		return pattern;
	}
	
	private static Op createNeedQuery(Model model, Statement parentStatement, Node newPredicate) {
		StatementBoundary boundary = new StatementBoundaryBase() {
			public boolean stopAt(Statement s) {
				return parentStatement.getSubject().equals(s.getSubject());
			}
		};
		
		Model subModel = new ModelExtract(boundary).extract(parentStatement.getObject().asResource(), model);
		BasicPattern pattern = createDetailsQuery(subModel);
		
		pattern.add(new Triple(resultName.asNode(), newPredicate, NodeFactory.createVariable(hashFunction(parentStatement.getObject()))));
		
		return new OpBGP(pattern);
	}

	protected void processActiveNeedEvent(NeedEvent needEvent) throws IOException, JsonLdError {
		log.info("Received active need.");

		String needURI = needEvent.getUri();

		Model model = needEvent.deserializeNeedDataset().getUnionModel();
		
		ArrayList<Op> queries = new ArrayList<>(2);
		
		Statement seeks = model.getProperty(model.createResource(needURI), model.createProperty("http://purl.org/webofneeds/model#seeks"));
		
		if(seeks != null) {
			Op seeksQuery = createNeedQuery(model, seeks, NodeFactory.createURI("http://purl.org/webofneeds/model#is"));
			
			queries.add(seeksQuery);
		}
		
		Statement is = model.getProperty(model.createResource(needURI), model.createProperty("http://purl.org/webofneeds/model#is"));
		
		if(is != null) {
			Op isQuery = createNeedQuery(model, is, NodeFactory.createURI("http://purl.org/webofneeds/model#seeks"));
			
			queries.add(isQuery);
		}
		
		queries.stream().reduce((left, right) -> new OpUnion(left, right))
		.ifPresent((union) -> {
			BasicPattern nodeUriBGP = new BasicPattern();
			Var wonNodeVar = Var.alloc("wonNode");
			nodeUriBGP.add(new Triple(resultName.asNode(), NodeFactory.createURI("http://purl.org/webofneeds/model#hasWonNode"), wonNodeVar.asNode()));
			
			Op query = new OpProject(OpJoin.create(new OpBGP(nodeUriBGP), union), Arrays.asList(new Var[]{resultName, wonNodeVar}));
			QueryExecution execution = QueryExecutionFactory.sparqlService(config.getSparqlEndpoint(), OpAsQuery.asQuery(query));
			
			ResultSet result = execution.execSelect();
			
			BulkHintEvent bulkHintEvent = new BulkHintEvent();
			
			while(result.hasNext()) {
				QuerySolution solution = result.nextSolution();
				String foundNeedURI = solution.get(resultName.getName()).toString();
				String foundNeedNodeURI = solution.get(wonNodeVar.getName()).toString();
				
				bulkHintEvent.addHintEvent(new HintEvent(needEvent.getWonNodeUri(), needURI, foundNeedNodeURI, foundNeedURI, config.getMatcherUri(), 1));
			}
			
			pubSubMediator.tell(new DistributedPubSubMediator.Publish(bulkHintEvent.getClass().getName(), bulkHintEvent), getSelf());
			
		});
	}

	@Override
	public SupervisorStrategy supervisorStrategy() {

		SupervisorStrategy supervisorStrategy = new OneForOneStrategy(0, Duration.Zero(),
				new Function<Throwable, SupervisorStrategy.Directive>() {

					@Override
					public SupervisorStrategy.Directive apply(Throwable t) throws Exception {

						log.warning("Actor encountered error: {}", t);
						// default behaviour
						return SupervisorStrategy.escalate();
					}
				});

		return supervisorStrategy;
	}

}
