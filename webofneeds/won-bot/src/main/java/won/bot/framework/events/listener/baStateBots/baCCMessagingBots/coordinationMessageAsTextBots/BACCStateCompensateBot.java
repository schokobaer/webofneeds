package won.bot.framework.events.listener.baStateBots.baCCMessagingBots.coordinationMessageAsTextBots;

import won.bot.framework.events.listener.baStateBots.BATestBotScript;
import won.bot.framework.events.listener.baStateBots.BATestScriptAction;
import won.node.facet.impl.WON_TX;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Danijel
 * Date: 26.2.14.
 * Time: 11.55
 * To change this template use File | Settings | File Templates.
 */


public class BACCStateCompensateBot extends BATestBotScript {

    @Override
    protected List<BATestScriptAction> setupActions() {
        List<BATestScriptAction> actions = new ArrayList();
        actions.add(new BATestScriptAction(false, "MESSAGE_COMPLETE", URI.create(WON_TX.STATE_ACTIVE.getURI())));
        actions.add(new BATestScriptAction(true, "MESSAGE_COMPLETED", URI.create(WON_TX.STATE_COMPLETING.getURI())));
        actions.add(new BATestScriptAction(false, "MESSAGE_COMPENSATE", URI.create(WON_TX.STATE_COMPLETED.getURI())));
        actions.add(new BATestScriptAction(true, "MESSAGE_COMPENSATED", URI.create(WON_TX.STATE_COMPENSATING.getURI())));
        return actions;
    }
}


