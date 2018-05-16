package won.bot.framework.eventbot.util;

import java.net.URI;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.springframework.util.StopWatch;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.behaviour.CrawlConnectionDataBehaviour;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.crawlconnection.CrawlConnectionCommandEvent;
import won.protocol.agreement.AgreementProtocolState;
import won.protocol.model.Connection;
import won.protocol.util.WonConversationUtils;
import won.protocol.util.WonRdfUtils;
import won.protocol.util.linkeddata.WonLinkedDataUtils;
import won.protocol.vocabulary.WON;
import won.protocol.vocabulary.WONPAY;

public class EventCrawler {

	private EventListenerContext eventListenerContext;

	public EventCrawler(EventListenerContext ctx) {
		eventListenerContext = ctx;
	}

	private String getDurationString(Duration queryDuration) {
		return new DecimalFormat("###.##").format(queryDuration.toMillis() / 1000d);
	}

	public interface MessageFinder {
		List<URI> findMessages(AgreementProtocolState state);
	}

	public interface MessageReferrer {
		Model referToMessages(Model messageModel, URI... targetUris);
	}

	public interface TextMessageMaker {
		String makeTextMessage(Duration queryDuration, AgreementProtocolState state, URI... uris);
	}

	public void referToEarlierMessages(EventListenerContext ctx, EventBus bus, Connection con,
			MessageFinder messageFinder, MessageReferrer messageReferrer, TextMessageMaker textMessageMaker) {
		// initiate crawl behaviour
		CrawlConnectionCommandEvent command = new CrawlConnectionCommandEvent(con.getNeedURI(), con.getConnectionURI());
		CrawlConnectionDataBehaviour crawlConnectionDataBehaviour = new CrawlConnectionDataBehaviour(ctx, command,
				Duration.ofSeconds(60));
		final StopWatch crawlStopWatch = new StopWatch();
		crawlStopWatch.start("crawl");
		AgreementProtocolState state = WonConversationUtils.getAgreementProtocolState(con.getConnectionURI(),
				ctx.getLinkedDataSource());
		crawlStopWatch.stop();
		Duration crawlDuration = Duration.ofMillis(crawlStopWatch.getLastTaskTimeMillis());
		// Model messageModel = WonRdfUtils.MessageUtils
		// .textMessage("Finished crawl in " + getDurationString(crawlDuration) + "
		// seconds. The dataset has "
		// + state.getConversationDataset().asDatasetGraph().size() + " rdf graphs.");
		// eventListenerContext.getEventBus().publish(new
		// ConnectionMessageCommandEvent(con, messageModel));
		Model messageModel = makeReferringMessage(state, messageFinder, messageReferrer, textMessageMaker);
		eventListenerContext.getEventBus().publish(new ConnectionMessageCommandEvent(con, messageModel));
		crawlConnectionDataBehaviour.activate();
	}

	private Model makeReferringMessage(AgreementProtocolState state, MessageFinder messageFinder,
			MessageReferrer messageReferrer, TextMessageMaker textMessageMaker) {
		int origPrio = Thread.currentThread().getPriority();
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		StopWatch queryStopWatch = new StopWatch();
		queryStopWatch.start("query");
		List<URI> targetUris = messageFinder.findMessages(state);
		URI[] targetUriArray = targetUris.toArray(new URI[targetUris.size()]);
		queryStopWatch.stop();
		Thread.currentThread().setPriority(origPrio);
		Duration queryDuration = Duration.ofMillis(queryStopWatch.getLastTaskTimeMillis());
		Model messageModel = WonRdfUtils.MessageUtils
				.textMessage(textMessageMaker.makeTextMessage(queryDuration, state, targetUriArray));
		return messageReferrer.referToMessages(messageModel, targetUriArray);
	}

	/**
	 * Crawls the events in the connection and searchs for payment details.
	 * 
	 * @param con
	 *            Connection to crawl through
	 * @param ctx
	 *            Context to get the data source
	 * @return Key Value Map with payment Details
	 */
	public static Map<String, String> crawlPaymentDetails(Connection con, EventListenerContext ctx) {
		Map<String, String> payDetails = new LinkedHashMap<>();
		AgreementProtocolState state = WonConversationUtils.getAgreementProtocolState(con.getConnectionURI(),
				ctx.getLinkedDataSource());

		// Sparql method
		// Dataset datasource = state.getConversationDataset();
		// String query = "select ?o \n"
		// + "where { \n"
		// + " ?s ?p ?o \n"
		// + "}";
		// try (QueryExecution qexec= QueryExecutionFactory.create(query, datasource)) {
		// ResultSet resultSet = qexec.execSelect();
		// while (resultSet.hasNext()) {
		// QuerySolution soln = resultSet.nextSolution();
		// RDFNode node = soln.get("o");
		// System.out.println(node.asLiteral().getString());
		// }
		// return payDetails;
		// }
		// catch (Exception e) {
		// e.printStackTrace();
		// }

		// Safe method
		Dataset dataset = state.getAgreements();
		Model agreements = dataset.getUnionModel();
		StmtIterator iterator = agreements.listStatements();
		while (iterator.hasNext()) {
			Statement stmt = iterator.next();
			Property prop = stmt.getPredicate();
			if (prop.equals(WON.HAS_TEXT_MESSAGE)) {
				RDFNode obj = stmt.getObject();
				String text = obj.asLiteral().getString();
				if (text.startsWith("pay_")) {
					int posEnd = text.indexOf(":", 4);
					if (posEnd > 0) {
						String key = text.substring(0, posEnd).toLowerCase();
						String val = text.substring(posEnd + 1).trim();
//						if (!payDetails.containsKey(key)) {
							payDetails.put(key, val);
//						}
					}
				}
			}
		}
		return payDetails;
	}
	
	/**
	 * Crawls the agreements for the latest payment.
	 * @param con
	 * @param ctx
	 * @return
	 */
	public static Resource getLatestPaymentAgreement(Connection con, EventListenerContext ctx) {
		AgreementProtocolState state = WonConversationUtils.getAgreementProtocolState(con.getConnectionURI(),
				ctx.getLinkedDataSource());
		
		// TODO: Implement with SPARQL
		/*
		 * SELECT ?payment
		 * WHERE {
		 *   ?payment a :Payment
		 * }
		 */
				
		Dataset dataset = state.getAgreements();
		Model agreements = dataset.getUnionModel();
		StmtIterator iterator = agreements.listStatements();
		while (iterator.hasNext()) {
			Statement stmt = iterator.next();
			Property prop = stmt.getPredicate();
			if (prop.equals(RDF.type)) {
				Resource obj = stmt.getObject().asResource(); 
				if (obj.equals(WONPAY.PAYPAL_PAYMENT) ||
						obj.equals(WONPAY.CASH_PAYMENT)) {
					return stmt.getSubject();
				}
			}
		}
		
		return null;
	}
	
	public static Resource getLatestPaymentReference(Connection con, EventListenerContext ctx) {
//		AgreementProtocolState state = WonConversationUtils.getAgreementProtocolState(con.getConnectionURI(),
//				ctx.getLinkedDataSource());
		
		Resource referer = null;
		
		Dataset dataset = WonLinkedDataUtils.getConversationAndNeedsDataset(con.getConnectionURI(), ctx.getLinkedDataSource());
		Model data = dataset.getUnionModel();
		
		StmtIterator iterator = data.listStatements();
		while (iterator.hasNext()) {
			Statement stmt = iterator.next();
			Property prop = stmt.getPredicate();
			if (prop.equals(WONPAY.REFERS_TO)) {
				referer = stmt.getSubject();
			}
		}
		
		return referer;
	}

}
