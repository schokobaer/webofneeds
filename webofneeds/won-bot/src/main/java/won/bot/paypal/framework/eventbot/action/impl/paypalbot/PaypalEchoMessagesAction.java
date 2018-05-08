package won.bot.paypal.framework.eventbot.action.impl.paypalbot;

import java.net.URI;

import org.apache.jena.query.Dataset;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.paypal.framework.eventbot.event.impl.paypalbot.PayPalEchoCommandEvent;
import won.protocol.exception.WonMessageBuilderException;
import won.protocol.message.WonMessage;
import won.protocol.message.WonMessageBuilder;
import won.protocol.service.WonNodeInformationService;
import won.protocol.util.WonRdfUtils;

public class PaypalEchoMessagesAction extends BaseEventBotAction {

	public PaypalEchoMessagesAction(EventListenerContext eventListenerContext) {
		super(eventListenerContext);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doRun(Event event, EventListener executingListener) throws Exception {
		
		if (event instanceof PayPalEchoCommandEvent) {
			PayPalEchoCommandEvent ppEvent = (PayPalEchoCommandEvent) event;
			String msg = ppEvent.getMsg();
			URI uri = ppEvent.getConnectionURI();
			WonMessage response = createWonMessage(uri, msg);
			getEventListenerContext().getWonMessageSender().sendWonMessage(response);
		}

	}
	
	private WonMessage createWonMessage(URI connectionURI, String message) throws WonMessageBuilderException {

	    WonNodeInformationService wonNodeInformationService =
	      getEventListenerContext().getWonNodeInformationService();

	    Dataset connectionRDF =
	      getEventListenerContext().getLinkedDataSource().getDataForResource(connectionURI);
	    URI remoteNeed = WonRdfUtils.ConnectionUtils.getRemoteNeedURIFromConnection(connectionRDF, connectionURI);
	    URI localNeed = WonRdfUtils.ConnectionUtils.getLocalNeedURIFromConnection(connectionRDF, connectionURI);
	    URI wonNode = WonRdfUtils.ConnectionUtils.getWonNodeURIFromConnection(connectionRDF, connectionURI);
	    Dataset remoteNeedRDF =
	      getEventListenerContext().getLinkedDataSource().getDataForResource(remoteNeed);

	    URI messageURI = wonNodeInformationService.generateEventURI(wonNode);

	    return WonMessageBuilder
	      .setMessagePropertiesForConnectionMessage(
	        messageURI,
	        connectionURI,
	        localNeed,
	        wonNode,
	        WonRdfUtils.ConnectionUtils.getRemoteConnectionURIFromConnection(connectionRDF, connectionURI),
	        remoteNeed,
	        WonRdfUtils.NeedUtils.getWonNodeURIFromNeed(remoteNeedRDF, remoteNeed),
	        message)
	      .build();
	  }

}
