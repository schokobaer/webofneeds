package won.node.facet.businessactivity;

import won.node.facet.impl.BAParticipantCompletionState;
import won.node.facet.impl.WON_BA;

import java.net.URI;
import java.util.ArrayList;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: Danijel
 * Date: 24.1.14.
 * Time: 16.08
 * To change this template use File | Settings | File Templates.
 */
public enum BAEventType {
    //in general, be permissive about messages where possible. Don't care about duplicate messages

    //close may always be called. It always closes the connnection.
    ///
    MESSAGE_CANCEL("MessageCancel", new ArrayList<BAPCParticipantState>(Arrays.asList(BAPCParticipantState.ACTIVE,
            BAPCParticipantState.CANCELING,  BAPCParticipantState.COMPLETED,
            BAPCParticipantState.CLOSING, BAPCParticipantState.COMPENSATING,
            BAPCParticipantState.FAILING_ACTIVE_CANCELING, BAPCParticipantState.FAILING_COMPENSATING,
            BAPCParticipantState.NOT_COMPLETING, BAPCParticipantState.EXITING,
            BAPCParticipantState.ENDED)), new ArrayList<BAPCCoordinatorState>(Arrays.asList(BAPCCoordinatorState.ACTIVE,
            BAPCCoordinatorState.CANCELING, BAPCCoordinatorState.COMPLETED,
            BAPCCoordinatorState.CLOSING, BAPCCoordinatorState.COMPENSATING,
            BAPCCoordinatorState.FAILING_ACTIVE_CANCELING, BAPCCoordinatorState.FAILING_COMPENSATING,
            BAPCCoordinatorState.NOT_COMPLETING, BAPCCoordinatorState.EXITING,
            BAPCCoordinatorState.ENDED))),

    MESSAGE_CLOSE("MessageClose", new ArrayList<BAPCParticipantState>(Arrays.asList(BAPCParticipantState.ACTIVE,
            BAPCParticipantState.CANCELING,  BAPCParticipantState.COMPLETED,
            BAPCParticipantState.CLOSING, BAPCParticipantState.COMPENSATING,
            BAPCParticipantState.FAILING_ACTIVE_CANCELING, BAPCParticipantState.FAILING_COMPENSATING,
            BAPCParticipantState.NOT_COMPLETING, BAPCParticipantState.EXITING,
            BAPCParticipantState.ENDED)), new ArrayList<BAPCCoordinatorState>(Arrays.asList(BAPCCoordinatorState.ACTIVE,
            BAPCCoordinatorState.CANCELING, BAPCCoordinatorState.COMPLETED,
            BAPCCoordinatorState.CLOSING, BAPCCoordinatorState.COMPENSATING,
            BAPCCoordinatorState.FAILING_ACTIVE_CANCELING, BAPCCoordinatorState.FAILING_COMPENSATING,
            BAPCCoordinatorState.NOT_COMPLETING, BAPCCoordinatorState.EXITING,
            BAPCCoordinatorState.ENDED))),

    MESSAGE_COMPENSATE("MessageCompensate", new ArrayList<BAPCParticipantState>(Arrays.asList(BAPCParticipantState.ACTIVE,
            BAPCParticipantState.CANCELING,  BAPCParticipantState.COMPLETED,
            BAPCParticipantState.CLOSING, BAPCParticipantState.COMPENSATING,
            BAPCParticipantState.FAILING_ACTIVE_CANCELING, BAPCParticipantState.FAILING_COMPENSATING,
            BAPCParticipantState.NOT_COMPLETING, BAPCParticipantState.EXITING,
            BAPCParticipantState.ENDED)), new ArrayList<BAPCCoordinatorState>(Arrays.asList(BAPCCoordinatorState.ACTIVE,
            BAPCCoordinatorState.CANCELING, BAPCCoordinatorState.COMPLETED,
            BAPCCoordinatorState.CLOSING, BAPCCoordinatorState.COMPENSATING,
            BAPCCoordinatorState.FAILING_ACTIVE_CANCELING, BAPCCoordinatorState.FAILING_COMPENSATING,
            BAPCCoordinatorState.NOT_COMPLETING, BAPCCoordinatorState.EXITING,
            BAPCCoordinatorState.ENDED))),

    MESSAGE_FAILED("MessageFailed", new ArrayList<BAPCParticipantState>(Arrays.asList(BAPCParticipantState.ACTIVE,
            BAPCParticipantState.CANCELING,  BAPCParticipantState.COMPLETED,
            BAPCParticipantState.CLOSING, BAPCParticipantState.COMPENSATING,
            BAPCParticipantState.FAILING_ACTIVE_CANCELING, BAPCParticipantState.FAILING_COMPENSATING,
            BAPCParticipantState.NOT_COMPLETING, BAPCParticipantState.EXITING,
            BAPCParticipantState.ENDED)), new ArrayList<BAPCCoordinatorState>(Arrays.asList(BAPCCoordinatorState.ACTIVE,
            BAPCCoordinatorState.CANCELING, BAPCCoordinatorState.COMPLETED,
            BAPCCoordinatorState.CLOSING, BAPCCoordinatorState.COMPENSATING,
            BAPCCoordinatorState.FAILING_ACTIVE_CANCELING, BAPCCoordinatorState.FAILING_COMPENSATING,
            BAPCCoordinatorState.NOT_COMPLETING, BAPCCoordinatorState.EXITING,
            BAPCCoordinatorState.ENDED))),

    MESSAGE_EXITED("MessageExited", new ArrayList<BAPCParticipantState>(Arrays.asList(BAPCParticipantState.ACTIVE,
            BAPCParticipantState.CANCELING,  BAPCParticipantState.COMPLETED,
            BAPCParticipantState.CLOSING, BAPCParticipantState.COMPENSATING,
            BAPCParticipantState.FAILING_ACTIVE_CANCELING, BAPCParticipantState.FAILING_COMPENSATING,
            BAPCParticipantState.NOT_COMPLETING, BAPCParticipantState.EXITING,
            BAPCParticipantState.ENDED)), new ArrayList<BAPCCoordinatorState>(Arrays.asList(BAPCCoordinatorState.ACTIVE,
            BAPCCoordinatorState.CANCELING, BAPCCoordinatorState.COMPLETED,
            BAPCCoordinatorState.CLOSING, BAPCCoordinatorState.COMPENSATING,
            BAPCCoordinatorState.FAILING_ACTIVE_CANCELING, BAPCCoordinatorState.FAILING_COMPENSATING,
            BAPCCoordinatorState.NOT_COMPLETING, BAPCCoordinatorState.EXITING,
            BAPCCoordinatorState.ENDED))),

    MESSAGE_NOTCOMPLETED("MessageNotCompleted", new ArrayList<BAPCParticipantState>(Arrays.asList(BAPCParticipantState.ACTIVE,
            BAPCParticipantState.CANCELING,  BAPCParticipantState.COMPLETED,
            BAPCParticipantState.CLOSING, BAPCParticipantState.COMPENSATING,
            BAPCParticipantState.FAILING_ACTIVE_CANCELING, BAPCParticipantState.FAILING_COMPENSATING,
            BAPCParticipantState.NOT_COMPLETING, BAPCParticipantState.EXITING,
            BAPCParticipantState.ENDED)), new ArrayList<BAPCCoordinatorState>(Arrays.asList(BAPCCoordinatorState.ACTIVE,
            BAPCCoordinatorState.CANCELING, BAPCCoordinatorState.COMPLETED,
            BAPCCoordinatorState.CLOSING, BAPCCoordinatorState.COMPENSATING,
            BAPCCoordinatorState.FAILING_ACTIVE_CANCELING, BAPCCoordinatorState.FAILING_COMPENSATING,
            BAPCCoordinatorState.NOT_COMPLETING, BAPCCoordinatorState.EXITING,
            BAPCCoordinatorState.ENDED))),



    MESSAGE_EXIT("MessageExit", new ArrayList<BAPCParticipantState>(Arrays.asList(BAPCParticipantState.ACTIVE,
            BAPCParticipantState.CANCELING,  BAPCParticipantState.COMPLETED,
            BAPCParticipantState.CLOSING, BAPCParticipantState.COMPENSATING,
            BAPCParticipantState.FAILING_ACTIVE_CANCELING, BAPCParticipantState.FAILING_COMPENSATING,
            BAPCParticipantState.NOT_COMPLETING, BAPCParticipantState.EXITING,
            BAPCParticipantState.ENDED)), new ArrayList<BAPCCoordinatorState>(Arrays.asList(BAPCCoordinatorState.ACTIVE,
            BAPCCoordinatorState.CANCELING, BAPCCoordinatorState.COMPLETED,
            BAPCCoordinatorState.CLOSING, BAPCCoordinatorState.COMPENSATING,
            BAPCCoordinatorState.FAILING_ACTIVE_CANCELING, BAPCCoordinatorState.FAILING_COMPENSATING,
            BAPCCoordinatorState.NOT_COMPLETING, BAPCCoordinatorState.EXITING,
            BAPCCoordinatorState.ENDED))),

    MESSAGE_COMPLETED("MessageCompleted", new ArrayList<BAPCParticipantState>(Arrays.asList(BAPCParticipantState.ACTIVE,
            BAPCParticipantState.CANCELING,  BAPCParticipantState.COMPLETED,
            BAPCParticipantState.CLOSING, BAPCParticipantState.COMPENSATING,
            BAPCParticipantState.FAILING_ACTIVE_CANCELING, BAPCParticipantState.FAILING_COMPENSATING,
            BAPCParticipantState.NOT_COMPLETING, BAPCParticipantState.EXITING,
            BAPCParticipantState.ENDED)), new ArrayList<BAPCCoordinatorState>(Arrays.asList(BAPCCoordinatorState.ACTIVE,
            BAPCCoordinatorState.CANCELING, BAPCCoordinatorState.COMPLETED,
            BAPCCoordinatorState.CLOSING, BAPCCoordinatorState.COMPENSATING,
            BAPCCoordinatorState.FAILING_ACTIVE_CANCELING, BAPCCoordinatorState.FAILING_COMPENSATING,
            BAPCCoordinatorState.NOT_COMPLETING, BAPCCoordinatorState.EXITING,
            BAPCCoordinatorState.ENDED))),

    MESSAGE_FAIL("MessageFail", new ArrayList<BAPCParticipantState>(Arrays.asList(BAPCParticipantState.ACTIVE,
            BAPCParticipantState.CANCELING,  BAPCParticipantState.COMPLETED,
            BAPCParticipantState.CLOSING, BAPCParticipantState.COMPENSATING,
            BAPCParticipantState.FAILING_ACTIVE_CANCELING, BAPCParticipantState.FAILING_COMPENSATING,
            BAPCParticipantState.NOT_COMPLETING, BAPCParticipantState.EXITING,
            BAPCParticipantState.ENDED)), new ArrayList<BAPCCoordinatorState>(Arrays.asList(BAPCCoordinatorState.ACTIVE,
            BAPCCoordinatorState.CANCELING, BAPCCoordinatorState.COMPLETED,
            BAPCCoordinatorState.CLOSING, BAPCCoordinatorState.COMPENSATING,
            BAPCCoordinatorState.FAILING_ACTIVE_CANCELING, BAPCCoordinatorState.FAILING_COMPENSATING,
            BAPCCoordinatorState.NOT_COMPLETING, BAPCCoordinatorState.EXITING,
            BAPCCoordinatorState.ENDED))),

    MESSAGE_CANNOTCOMPLETE("MessageComplete", new ArrayList<BAPCParticipantState>(Arrays.asList(BAPCParticipantState.ACTIVE,
            BAPCParticipantState.CANCELING,  BAPCParticipantState.COMPLETED,
            BAPCParticipantState.CLOSING, BAPCParticipantState.COMPENSATING,
            BAPCParticipantState.FAILING_ACTIVE_CANCELING, BAPCParticipantState.FAILING_COMPENSATING,
            BAPCParticipantState.NOT_COMPLETING, BAPCParticipantState.EXITING,
            BAPCParticipantState.ENDED)), new ArrayList<BAPCCoordinatorState>(Arrays.asList(BAPCCoordinatorState.ACTIVE,
            BAPCCoordinatorState.CANCELING, BAPCCoordinatorState.COMPLETED,
            BAPCCoordinatorState.CLOSING, BAPCCoordinatorState.COMPENSATING,
            BAPCCoordinatorState.FAILING_ACTIVE_CANCELING, BAPCCoordinatorState.FAILING_COMPENSATING,
            BAPCCoordinatorState.NOT_COMPLETING, BAPCCoordinatorState.EXITING,
            BAPCCoordinatorState.ENDED))),

    MESSAGE_CANCELED("MessageCanceled", new ArrayList<BAPCParticipantState>(Arrays.asList(BAPCParticipantState.ACTIVE,
            BAPCParticipantState.CANCELING,  BAPCParticipantState.COMPLETED,
            BAPCParticipantState.CLOSING, BAPCParticipantState.COMPENSATING,
            BAPCParticipantState.FAILING_ACTIVE_CANCELING, BAPCParticipantState.FAILING_COMPENSATING,
            BAPCParticipantState.NOT_COMPLETING, BAPCParticipantState.EXITING,
            BAPCParticipantState.ENDED)), new ArrayList<BAPCCoordinatorState>(Arrays.asList(BAPCCoordinatorState.ACTIVE,
            BAPCCoordinatorState.CANCELING, BAPCCoordinatorState.COMPLETED,
            BAPCCoordinatorState.CLOSING, BAPCCoordinatorState.COMPENSATING,
            BAPCCoordinatorState.FAILING_ACTIVE_CANCELING, BAPCCoordinatorState.FAILING_COMPENSATING,
            BAPCCoordinatorState.NOT_COMPLETING, BAPCCoordinatorState.EXITING,
            BAPCCoordinatorState.ENDED))),

    MESSAGE_CLOSED("MessageClosed", new ArrayList<BAPCParticipantState>(Arrays.asList(BAPCParticipantState.ACTIVE,
            BAPCParticipantState.CANCELING,  BAPCParticipantState.COMPLETED,
            BAPCParticipantState.CLOSING, BAPCParticipantState.COMPENSATING,
            BAPCParticipantState.FAILING_ACTIVE_CANCELING, BAPCParticipantState.FAILING_COMPENSATING,
            BAPCParticipantState.NOT_COMPLETING, BAPCParticipantState.EXITING,
            BAPCParticipantState.ENDED)), new ArrayList<BAPCCoordinatorState>(Arrays.asList(BAPCCoordinatorState.ACTIVE,
            BAPCCoordinatorState.CANCELING, BAPCCoordinatorState.COMPLETED,
            BAPCCoordinatorState.CLOSING, BAPCCoordinatorState.COMPENSATING,
            BAPCCoordinatorState.FAILING_ACTIVE_CANCELING, BAPCCoordinatorState.FAILING_COMPENSATING,
            BAPCCoordinatorState.NOT_COMPLETING, BAPCCoordinatorState.EXITING,
            BAPCCoordinatorState.ENDED))),

    MESSAGE_COMPENSATED("MessageCompensated", new ArrayList<BAPCParticipantState>(Arrays.asList(BAPCParticipantState.ACTIVE,
            BAPCParticipantState.CANCELING,  BAPCParticipantState.COMPLETED,
            BAPCParticipantState.CLOSING, BAPCParticipantState.COMPENSATING,
            BAPCParticipantState.FAILING_ACTIVE_CANCELING, BAPCParticipantState.FAILING_COMPENSATING,
            BAPCParticipantState.NOT_COMPLETING, BAPCParticipantState.EXITING,
            BAPCParticipantState.ENDED)), new ArrayList<BAPCCoordinatorState>(Arrays.asList(BAPCCoordinatorState.ACTIVE,
            BAPCCoordinatorState.CANCELING, BAPCCoordinatorState.COMPLETED,
            BAPCCoordinatorState.CLOSING, BAPCCoordinatorState.COMPENSATING,
            BAPCCoordinatorState.FAILING_ACTIVE_CANCELING, BAPCCoordinatorState.FAILING_COMPENSATING,
            BAPCCoordinatorState.NOT_COMPLETING, BAPCCoordinatorState.EXITING,
            BAPCCoordinatorState.ENDED)));



    private String name;
    private ArrayList<BAPCParticipantState> permittingPStates;
    private ArrayList<BAPCCoordinatorState> permittingCStates;

    BAEventType(String name, ArrayList<BAPCParticipantState> permittingPStates, ArrayList<BAPCCoordinatorState> perimttingCStates) {
        this.permittingPStates = permittingPStates;
        this.permittingCStates = perimttingCStates;
        this.name = name;
    }

    public boolean isMessageAllowed(BAPCParticipantState stateToCheck){
        if (this.permittingPStates.contains(stateToCheck))
            return true;
        else
            return false;
    }

    public boolean isMessageAllowed(BAPCCoordinatorState stateToCheck){
        if (this.permittingCStates.contains(stateToCheck))
            return true;
        else
            return false;
    }

    public URI getURI() {
        return URI.create(WON_BA.BASE_URI + name);
    }

    public BAEventType getBAEventTypeFromURIParticipantInbound (String sURI)
    {
        for (BAEventType eventType: BAEventType.values()){
            if (sURI.equals(eventType.getURI().toString())) return eventType;
        }
        return null;
        /*if(sURI.toString().equals(WON_BA.MESSAGE_CANCEL.toString()))
            return BAEventType.PARTICIPANT_INBOUND_CANCEL;
        else if(sURI.toString().equals(BAEventType.PARTICIPANT_INBOUND_CLOSE.getURI().toString()))
            return BAEventType.PARTICIPANT_INBOUND_CLOSE;
        else if(sURI.toString().equals((BAEventType.PARTICIPANT_INBOUND_COMPENSATE.getURI().toString())))
            return BAEventType.PARTICIPANT_INBOUND_COMPENSATE;
        else return null;   */



    }
}


