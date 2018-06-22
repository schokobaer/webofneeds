import { parseMessage } from "./parse-message.js";
import { markUriAsRead } from "../../won-localstorage.js";
import { markConnectionAsRead } from "./reduce-connections.js";

/*
 "alreadyProcessed" flag, which indicates that we do not care about the
 sent status anymore and assume that it has been successfully sent to each server (incl. the remote)
 */
export function addMessage(state, wonMessage, alreadyProcessed = false) {
  if (wonMessage.getContentGraphs().length > 0) {
    // we only want to add messages to the state that actually contain text
    // content. (no empty connect messages, for example)
    let parsedMessage = parseMessage(wonMessage, alreadyProcessed);

    if (parsedMessage) {
      const connectionUri = parsedMessage.get("belongsToUri");
      let needUri = null;
      if (parsedMessage.getIn(["data", "outgoingMessage"])) {
        // needUri is the message's hasSenderNeed
        needUri = wonMessage.getSenderNeed();
      } else {
        // needUri is the remote message's hasReceiverNeed
        needUri = wonMessage.getReceiverNeed();
        if (parsedMessage.getIn(["data", "unread"])) {
          //If there is a new message for the connection we will set the connection to newConnection
          state = state.setIn(
            [needUri, "lastUpdateDate"],
            parsedMessage.getIn(["data", "date"])
          );
          state = state.setIn([needUri, "unread"], true);
          state = state.setIn(
            [needUri, "connections", connectionUri, "lastUpdateDate"],
            parsedMessage.getIn(["data", "date"])
          );
          state = state.setIn(
            [needUri, "connections", connectionUri, "unread"],
            true
          );
        }
      }
      if (needUri) {
        let messages = state.getIn([
          needUri,
          "connections",
          connectionUri,
          "messages",
        ]);
        messages = messages.set(
          parsedMessage.getIn(["data", "uri"]),
          parsedMessage.get("data")
        );
        return state.setIn(
          [needUri, "connections", connectionUri, "messages"],
          messages
        );
      }
    }
  }
  return state;
}

/*
 This method should only be called to for messages that are already stored on the server (reload, initial login etc)
 because we will add all the messages with the "alreadyProcessed" flag, which indicates that we do not care about the
 sent status anymore and assume that it has been successfully sent to each server (incl. the remote)
 */
export function addExistingMessages(state, wonMessages) {
  if (wonMessages && wonMessages.size > 0) {
    wonMessages.map(wonMessage => {
      state = addMessage(state, wonMessage, true);
    });
  } else {
    console.log("no messages to add");
  }
  return state;
}

export function markMessageAsRead(state, messageUri, connectionUri, needUri) {
  const need = state.get(needUri);
  const connection = need && need.getIn(["connections", connectionUri]);
  const message = connection && connection.getIn(["messages", messageUri]);

  markUriAsRead(messageUri);

  if (!message) {
    console.error(
      "no message with messageUri: <",
      messageUri,
      "> found within needUri: <",
      needUri,
      "> connectionUri: <",
      connectionUri,
      ">"
    );
    return state;
  }

  state = state.setIn(
    [needUri, "connections", connectionUri, "messages", messageUri, "unread"],
    false
  );

  if (
    state
      .getIn([needUri, "connections", connectionUri, "messages"])
      .filter(msg => msg.get("unread")).size == 0
  ) {
    state = markConnectionAsRead(state, connectionUri, needUri);
  }

  return state.setIn(
    [needUri, "connections", connectionUri, "messages", messageUri, "unread"],
    false
  );
}

export function markMessageAsRelevant(
  state,
  messageUri,
  connectionUri,
  needUri,
  relevant
) {
  let need = state.get(needUri);
  let connection = need && need.getIn(["connections", connectionUri]);
  let message = connection && connection.getIn(["messages", messageUri]);

  if (!message) {
    console.error(
      "no message with messageUri: <",
      messageUri,
      "> found within needUri: <",
      needUri,
      "> connectionUri: <",
      connectionUri,
      ">"
    );
    return state;
  }
  return state.setIn(
    [
      needUri,
      "connections",
      connectionUri,
      "messages",
      messageUri,
      "isRelevant",
    ],
    relevant
  );
}