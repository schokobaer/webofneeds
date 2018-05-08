package won.bot.framework.eventbot.event.impl.paypalbot;

import won.bot.framework.eventbot.event.impl.debugbot.DebugCommandEvent;
import won.protocol.model.Connection;

public class PayPalEchoCommandEvent extends DebugCommandEvent {

	private String msg;
	
	public PayPalEchoCommandEvent(Connection con, String msg) {
		super(con);
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}
}
