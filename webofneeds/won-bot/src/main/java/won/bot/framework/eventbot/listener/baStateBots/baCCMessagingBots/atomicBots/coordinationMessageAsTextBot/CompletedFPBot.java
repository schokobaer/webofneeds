package won.bot.framework.eventbot.listener.baStateBots.baCCMessagingBots.atomicBots.coordinationMessageAsTextBot;

import won.bot.framework.eventbot.listener.baStateBots.BATestBotScript;
import won.bot.framework.eventbot.listener.baStateBots.BATestScriptAction;
import won.node.facet.impl.WON_TX;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Danijel
 * Date: 17.4.14.
 */
public class CompletedFPBot extends BATestBotScript
{

  @Override
  protected List<BATestScriptAction> setupActions() {
    List<BATestScriptAction> actions = new ArrayList();

    actions.add(new BATestScriptAction(false, "MESSAGE_COMPLETE", URI.create(WON_TX.STATE_ACTIVE.getURI())));
    actions.add(new BATestScriptAction(true, "MESSAGE_COMPLETED", URI.create(WON_TX.STATE_COMPLETING.getURI())));

    return actions;
  }
}
