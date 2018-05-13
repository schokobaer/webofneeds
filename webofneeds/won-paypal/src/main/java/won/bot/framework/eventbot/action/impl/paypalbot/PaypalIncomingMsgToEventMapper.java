package won.bot.framework.eventbot.action.impl.paypalbot;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.BaseNeedAndConnectionSpecificEvent;
import won.bot.framework.eventbot.event.ConnectionSpecificEvent;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.MessageEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.paypalbot.PayPalEchoCommandEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.eventbot.util.EventCrawler;
import won.bot.framework.eventbot.util.PaymentUtil;
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
            
            handleMsg(message, con, bus);
		}

	}
	
	private void handleMsg(String cmd, Connection con, EventBus bus) {
		EventListenerContext ctx = getEventListenerContext();
        
        if (cmd.startsWith("echo ")) {
        	String echo = cmd.substring(5);
        	bus.publish(new PayPalEchoCommandEvent(con, echo));
        }
        else if (cmd.equals("usage")) {
        	bus.publish(new PayPalEchoCommandEvent(con, "echo: Repeats everything behind the echo"));
        	bus.publish(new PayPalEchoCommandEvent(con, "accept: Accepts the last proposal"));
        	bus.publish(new PayPalEchoCommandEvent(con, "payment receiver: Sends the receiver address of the bot and proposes it to you"));
        	bus.publish(new PayPalEchoCommandEvent(con, "payment types: Sends a list of the supported payment types of the bot"));
        	bus.publish(new PayPalEchoCommandEvent(con, "payment validate: Validates the accepted payment messages, summarizes them and proposes that"));
        	bus.publish(new PayPalEchoCommandEvent(con, "payment check: Checks if the last payment is complete"));
        }
        else if (cmd.equals("accept")) {
        	accept(ctx, bus, con);
        }
        else if (cmd.equals("payment receiver")) {
        	// TODO: Implement
        	String msg = "pay_rec: test@won.org";
        	bus.publish(new PayPalEchoCommandEvent(con, msg));
        	propose(ctx, bus, con, true, false, 1);
        }
        else if (cmd.equals("payment types")) {
        	bus.publish(new PayPalEchoCommandEvent(con, "pay_type: PaypalPayment"));
        	bus.publish(new PayPalEchoCommandEvent(con, "pay_type: Cash"));
        }
        else if (cmd.equals("payment validate")) {
        	// TODO: Implement
        	//bus.publish(new PayPalEchoCommandEvent(con, "Validation is not implemented yet..."));
        	validate(ctx, bus, con);
        }
        else if (cmd.equals("payment check")) {
        	// TODO: Implement
        	bus.publish(new PayPalEchoCommandEvent(con, "Checking is not implemented yet..."));
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
	
	private void propose(EventListenerContext ctx, EventBus bus, Connection con, boolean allowOwnClauses, boolean allowCounterpartClauses, int count) {
		String whose = allowOwnClauses ? allowCounterpartClauses ? "our" : "my" : allowCounterpartClauses ? "your" : " - sorry, don't know which ones to choose, actually - ";  
		crawler.referToEarlierMessages(ctx, bus, con, 
				state -> {
					return state.getNLatestMessageUris(m -> {
						URI ownNeedUri = con.getNeedURI();
						URI remoteNeedUri = con.getRemoteNeedURI();
						return 
								ownNeedUri != null && ownNeedUri.equals(m.getSenderNeedURI()) && allowOwnClauses || 
						   	    remoteNeedUri != null && remoteNeedUri.equals(m.getSenderNeedURI()) && allowCounterpartClauses;
								
					},count).subList(0, count);
				}, 
				(messageModel, uris) -> WonRdfUtils.MessageUtils.addProposes(messageModel, uris),
				(Duration queryDuration, AgreementProtocolState state, URI... uris) -> {
					if (uris == null || uris.length == 0 || uris[0] == null) {
						return "Sorry, I cannot propose the messages - I did not find any.";
					}
					Optional<String> proposedString = state.getTextMessage(uris[0]);
			        return "Ok, I am hereby making the proposal, containing " + uris.length + " clauses.";
				});
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
	
	private void validate(EventListenerContext ctx, EventBus bus, Connection con) {
		
		Model model = null;
		
		try {
			model = PaymentUtil.generateModelByAgreements(con, ctx);
		} catch (Exception e) {
			model = WonRdfUtils.MessageUtils.textMessage(e.getMessage());
		}
		
		bus.publish(new ConnectionMessageCommandEvent(con, model));
	}

}
