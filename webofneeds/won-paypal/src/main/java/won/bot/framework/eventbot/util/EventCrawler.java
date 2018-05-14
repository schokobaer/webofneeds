package won.bot.framework.eventbot.util;

import java.net.URI;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
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
import won.protocol.vocabulary.WON;

public class EventCrawler {

	private EventListenerContext eventListenerContext;
	
	public EventCrawler(EventListenerContext ctx) {
		eventListenerContext = ctx;
	}
	
	private String getDurationString(Duration queryDuration) {
		return new DecimalFormat("###.##").format( queryDuration.toMillis() / 1000d);
	}
	
	public interface MessageFinder{
    	List<URI> findMessages(AgreementProtocolState state);
    }
    
	public interface MessageReferrer {
    	Model referToMessages(Model messageModel, URI... targetUris);
    }
    
	public interface TextMessageMaker{
    	String makeTextMessage(Duration queryDuration, AgreementProtocolState state, URI...uris);
    }
   

	public void referToEarlierMessages(EventListenerContext ctx, EventBus bus, Connection con, MessageFinder messageFinder, MessageReferrer messageReferrer, TextMessageMaker textMessageMaker) {
		// initiate crawl behaviour
		CrawlConnectionCommandEvent command = new CrawlConnectionCommandEvent(con.getNeedURI(), con.getConnectionURI());
		CrawlConnectionDataBehaviour crawlConnectionDataBehaviour = new CrawlConnectionDataBehaviour(ctx, command, Duration.ofSeconds(60));
		final StopWatch crawlStopWatch = new StopWatch();
		crawlStopWatch.start("crawl");
		AgreementProtocolState state = WonConversationUtils.getAgreementProtocolState(con.getConnectionURI(), ctx.getLinkedDataSource());
		crawlStopWatch.stop();
		Duration crawlDuration = Duration.ofMillis(crawlStopWatch.getLastTaskTimeMillis());
//		Model messageModel = WonRdfUtils.MessageUtils
//				.textMessage("Finished crawl in " + getDurationString(crawlDuration) + " seconds. The dataset has "
//						+ state.getConversationDataset().asDatasetGraph().size() + " rdf graphs.");
//		eventListenerContext.getEventBus().publish(new ConnectionMessageCommandEvent(con, messageModel));
		Model messageModel = makeReferringMessage(state, messageFinder, messageReferrer, textMessageMaker);	
		eventListenerContext.getEventBus().publish(new ConnectionMessageCommandEvent(con, messageModel));
		crawlConnectionDataBehaviour.activate();
	}
    
    private Model makeReferringMessage(AgreementProtocolState state, MessageFinder messageFinder, MessageReferrer messageReferrer, TextMessageMaker textMessageMaker) {
		int origPrio = Thread.currentThread().getPriority();
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		StopWatch queryStopWatch = new StopWatch();
		queryStopWatch.start("query");
		List<URI> targetUris = messageFinder.findMessages(state); 
		URI[] targetUriArray = targetUris.toArray(new URI[targetUris.size()]);
		queryStopWatch.stop();
		Thread.currentThread().setPriority(origPrio);
		Duration queryDuration = Duration.ofMillis(queryStopWatch.getLastTaskTimeMillis());
        Model messageModel = WonRdfUtils.MessageUtils.textMessage(textMessageMaker.makeTextMessage(queryDuration, state, targetUriArray));
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
		AgreementProtocolState state = WonConversationUtils.getAgreementProtocolState(con.getConnectionURI(),
				ctx.getLinkedDataSource());
		Dataset dataset = state.getAgreements();
		Model agreements = dataset.getUnionModel();
		Map<String, String> payDetails = new LinkedHashMap<>();
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
						payDetails.put(key, val);
					}
				}
			}
		}
		return payDetails;
	}

}
