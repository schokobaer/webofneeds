package won.bot.framework.eventbot.action.impl.paypalbot;

import org.apache.commons.lang3.StringUtils;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.BaseNeedAndConnectionSpecificEvent;
import won.bot.framework.eventbot.event.ConnectionSpecificEvent;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.MessageEvent;
import won.bot.framework.eventbot.event.impl.paypalbot.PayPalEchoCommandEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.message.WonMessage;
import won.protocol.model.Connection;
import won.protocol.util.WonRdfUtils;

public class MyPaypalEventBotListenerAction extends BaseEventBotAction {

	public MyPaypalEventBotListenerAction(EventListenerContext eventListenerContext) {
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
            
            if (message.startsWith("paypal:")) {
            	String echo = message.substring(7);
            	// TODO: Uncomment the next line:
            	bus.publish(new PayPalEchoCommandEvent(con, echo));
            	//bus.publish(new MessageToElizaEvent(con, "PayPal says: "+ message));
            }
            else {
            	//bus.publish(new MessageToElizaEvent(con, "Eliza says: "+ message));
            }
		}

	}

	private String extractTextMessageFromWonMessage(WonMessage wonMessage) {
		if (wonMessage == null)
			return null;
		String message = WonRdfUtils.MessageUtils.getTextMessage(wonMessage);
		return StringUtils.trim(message);
	}

}
