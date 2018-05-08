package won.bot.paypal.framework.eventbot.crawler;

import java.net.URI;
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

public class CrawlUtils {

	private BotActionHolder elcHolder;
	
	public CrawlUtils(BotActionHolder holder) {
		elcHolder = holder;
	}
	
	private interface MessageFinder{
    	List<URI> findMessages(AgreementProtocolState state);
    }
    
    private interface MessageReferrer {
    	Model referToMessages(Model messageModel, URI... targetUris);
    }
    
    private interface TextMessageMaker{
    	String makeTextMessage(Duration queryDuration, AgreementProtocolState state, URI...uris);
    }
	
	private void referToEarlierMessages(EventListenerContext ctx, EventBus bus, Connection con, String crawlAnnouncement, MessageFinder messageFinder, MessageReferrer messageReferrer, TextMessageMaker textMessageMaker) {
		Model messageModel = WonRdfUtils.MessageUtils
		        .textMessage(crawlAnnouncement);
		bus.publish(new ConnectionMessageCommandEvent(con, messageModel));
		
		// initiate crawl behaviour
		CrawlConnectionCommandEvent command = new CrawlConnectionCommandEvent(con.getNeedURI(), con.getConnectionURI());
		CrawlConnectionDataBehaviour crawlConnectionDataBehaviour = new CrawlConnectionDataBehaviour(ctx, command, Duration.ofSeconds(60));
		final StopWatch crawlStopWatch = new StopWatch();
		crawlStopWatch.start("crawl");
		AgreementProtocolState state = WonConversationUtils.getAgreementProtocolState(con.getConnectionURI(), ctx.getLinkedDataSource());
		crawlStopWatch.stop();
		Duration crawlDuration = Duration.ofMillis(crawlStopWatch.getLastTaskTimeMillis());
		messageModel = WonRdfUtils.MessageUtils
				.textMessage("Finished crawl in " + elcHolder.getDurationString(crawlDuration) + " seconds. The dataset has "
						+ state.getConversationDataset().asDatasetGraph().size() + " rdf graphs.");
		elcHolder.eventListenerContext().getEventBus().publish(new ConnectionMessageCommandEvent(con, messageModel));
		messageModel = makeReferringMessage(state, messageFinder, messageReferrer, textMessageMaker);	
		elcHolder.eventListenerContext().getEventBus().publish(new ConnectionMessageCommandEvent(con, messageModel));
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
