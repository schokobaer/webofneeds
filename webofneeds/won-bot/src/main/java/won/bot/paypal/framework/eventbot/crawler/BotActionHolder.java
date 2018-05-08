package won.bot.paypal.framework.eventbot.crawler;

import java.time.Duration;

import won.bot.framework.eventbot.EventListenerContext;

public interface BotActionHolder {
	EventListenerContext eventListenerContext();
	String getDurationString(Duration crawlDuration);
}
