package won.bot.framework.eventbot.crawl;

import java.net.URI;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.List;

import org.apache.jena.rdf.model.Model;
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
		Model messageModel = WonRdfUtils.MessageUtils
				.textMessage("Finished crawl in " + getDurationString(crawlDuration) + " seconds. The dataset has "
						+ state.getConversationDataset().asDatasetGraph().size() + " rdf graphs.");
		eventListenerContext.getEventBus().publish(new ConnectionMessageCommandEvent(con, messageModel));
		messageModel = makeReferringMessage(state, messageFinder, messageReferrer, textMessageMaker);	
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

}
