package won.bot.framework.eventbot.action.impl.paypalbot;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.crawl.EventCrawler;
import won.bot.framework.eventbot.event.BaseNeedAndConnectionSpecificEvent;
import won.bot.framework.eventbot.event.ConnectionSpecificEvent;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.MessageEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.paypalbot.PayPalEchoCommandEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.agreement.AgreementProtocolState;
import won.protocol.message.WonMessage;
import won.protocol.model.Connection;
import won.protocol.util.WonRdfUtils;

public class PaypalIncomingMsgToEventMapper extends BaseEventBotAction {

	private EventCrawler crawler = new EventCrawler(getEventListenerContext());
	
	public PaypalIncomingMsgToEventMapper(EventListenerContext eventListenerContext) {
		super(eventListenerContext);
	}

	@Override
	protected void doRun(Event event, EventListener executingListener) throws Exception {
		ConnectionSpecificEvent messageEvent = (ConnectionSpecificEvent)event;
		if(messageEvent instanceof MessageEvent) {
			EventListenerContext ctx = getEventListenerContext();
            EventBus bus = ctx.getEventBus();

            Connection con = ((BaseNeedAndConnectionSpecificEvent) messageEvent).getCon();
            WonMessage msg = ((MessageEvent) messageEvent).getWonMessage();
            String message = extractTextMessageFromWonMessage(msg);
            
            if (message.startsWith("paypal ")) {
            	String cmd = message.substring(7);
            	handleMsg(cmd, con, bus);
            }
		}

	}
	
	private void handleMsg(String cmd, Connection con, EventBus bus) {
		EventListenerContext ctx = getEventListenerContext();
        
        if (cmd.startsWith("echo ")) {
        	String echo = cmd.substring(5);
        	bus.publish(new PayPalEchoCommandEvent(con, echo));
        }
        else if (cmd.startsWith("accept ")) {
        	accept(ctx, bus, con);
        }
        else if (cmd.startsWith("asdf")) {
        	accept(ctx, bus, con);
        }
	}

	private String extractTextMessageFromWonMessage(WonMessage wonMessage) {
		if (wonMessage == null)
			return null;
		String message = WonRdfUtils.MessageUtils.getTextMessage(wonMessage);
		return StringUtils.trim(message);
	}
	
	private void sendSimpleMsg(String msg, EventBus bus, Connection con) {
		Model messageModel = WonRdfUtils.MessageUtils
		        .textMessage(msg);
		bus.publish(new ConnectionMessageCommandEvent(con, messageModel));
	}
	
	private void accept(EventListenerContext ctx, EventBus bus, Connection con) {
		crawler.referToEarlierMessages(ctx, bus, con, 
				state -> {
					URI uri = state.getLatestPendingProposal(Optional.empty(), Optional.of(con.getRemoteNeedURI()));
					return uri == null ? Collections.EMPTY_LIST : Arrays.asList(uri);
				}, 
				(messageModel, uris) -> WonRdfUtils.MessageUtils.addAccepts(messageModel, uris),
				(Duration queryDuration, AgreementProtocolState state, URI... uris) -> {
					if (uris == null || uris.length == 0 || uris[0] == null) {
						return "Sorry, I cannot accept any proposal - I did not find pending proposals";
					}
			        return "Ok, I am hereby accepting your latest proposal (uri: " + uris[0]+").";
				});
	}

}
