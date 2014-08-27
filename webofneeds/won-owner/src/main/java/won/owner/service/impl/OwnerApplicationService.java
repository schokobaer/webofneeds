package won.owner.service.impl;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import won.owner.service.OwnerApplicationServiceCallback;
import won.owner.service.OwnerProtocolOwnerServiceCallback;
import won.protocol.exception.MultipleQueryResultsFoundException;
import won.protocol.message.WonMessage;
import won.protocol.message.WonMessageBuilder;
import won.protocol.message.WonMessageDecoder;
import won.protocol.message.WonMessageType;
import won.protocol.model.ChatMessage;
import won.protocol.model.Connection;
import won.protocol.model.Match;
import won.protocol.owner.OwnerProtocolNeedServiceClientSide;
import won.protocol.repository.ConnectionRepository;
import won.protocol.repository.NeedRepository;
import won.protocol.util.WonRdfUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;

/**
 * User: fsalcher
 * Date: 18.08.2014
 */
public class OwnerApplicationService implements OwnerProtocolOwnerServiceCallback
{

  private static final Logger logger = LoggerFactory.getLogger(OwnerApplicationService.class);

  @Autowired
  @Qualifier("default")
  private OwnerProtocolNeedServiceClientSide ownerProtocolService;

  @Autowired
  private OwnerApplicationServiceCallback ownerApplicationServiceCallbackToClient =
      new NopOwnerApplicationServiceCallback();

  @Autowired
  private ConnectionRepository connectionRepository;

  @Autowired
  private NeedRepository needRepository;


  // ToDo (FS): add security layer

  public void handleMessageEventFromClient(Dataset wonMessage)
  {
    handleMessageEventFromClient(WonMessageDecoder.decodeFromDataset(wonMessage));
  }

  public void handleMessageEventFromClient(WonMessage wonMessage)
  {

    // ToDo (FS): don't convert messages to the old protocol interfaces instead use the new message format

    WonMessageType wonMessageType = wonMessage.getMessageEvent().getMessageType();

    switch (wonMessageType) {
      case CREATE_NEED:

        Dataset messageContent = wonMessage.getMessageContent();

        URI senderURI = wonMessage.getMessageEvent().getSenderURI();

        // ToDo (FS): maybe sender should be included in each message to retrieve the needURI

        // get the core graph of the message for the need model
        String coreModelURIString = senderURI.toString() + "/core#data";
        Model content = wonMessage.getMessageContent(coreModelURIString);

        // get the active status
        boolean active = false;
        try {
          active = WonRdfUtils.NeedUtils.queryActiveStatus(messageContent);
        } catch (MultipleQueryResultsFoundException e) {
          logger.warn("caught MultipleOwnersFoundException:", e);
        }

        // get the wonNodeURI
        URI wonNodeURI = null;
        try {
          wonNodeURI = WonRdfUtils.NeedUtils.queryWonNode(messageContent);
        } catch (MultipleQueryResultsFoundException e) {
          logger.warn("caught MultipleOwnersFoundException:", e);
        }

        try {
          ownerProtocolService.createNeed(content, active, wonNodeURI, wonMessage);
        } catch (Exception e) {
          logger.warn("caught Exception:", e);
        }

        // ToDo (FS): WON Node should do this
        sendBackResponseMessageToClient(wonMessage);

        break;

      case CONNECT:
        try {
          URI needURI;
          URI otherNeedURI;

          needURI = wonMessage.getMessageEvent().getSenderURI();
          otherNeedURI = wonMessage.getMessageEvent().getReceiverURI();

          content = wonMessage.getMessageEvent().getModel();

          // ToDo (FS): change connect code such that the connectionID of the messageEvent will be used
          ownerProtocolService.connect(needURI, otherNeedURI, content, null);
        } catch (Exception e) {
          logger.warn("caught Exception", e);
        }
        break;

      case NEED_STATE:
        try {
          URI needURI;
          needURI = wonMessage.getMessageEvent().getSenderURI();

          switch (wonMessage.getMessageEvent().getNewNeedState()) {
            case ACTIVE:
              ownerProtocolService.activate(needURI, null);
              break;
            case INACTIVE:
              ownerProtocolService.deactivate(needURI, null);
          }
        } catch (Exception e) {
          logger.warn("caught Exception", e);
        }
        break;

      case OPEN:
        try {

          senderURI = wonMessage.getMessageEvent().getSenderURI();
          URI receiverURI = wonMessage.getMessageEvent().getReceiverURI();

          List<Connection> connections =
              connectionRepository.findByNeedURIAndRemoteNeedURI(senderURI, receiverURI);

          URI connectionURI = connections.get(0).getConnectionURI();

          content = wonMessage.getMessageEvent().getModel();

          ownerProtocolService.open(connectionURI, content, null);
        } catch (Exception e) {
          logger.warn("caught Exception", e);
        }
        break;

      case CLOSE:
        try {

          senderURI = wonMessage.getMessageEvent().getSenderURI();
          URI receiverURI = wonMessage.getMessageEvent().getReceiverURI();

          List<Connection> connections =
              connectionRepository.findByNeedURIAndRemoteNeedURI(senderURI, receiverURI);

          URI connectionURI = connections.get(0).getConnectionURI();

          content = wonMessage.getMessageEvent().getModel();

          ownerProtocolService.close(connectionURI, content, null);
        } catch (Exception e) {
          logger.warn("caught Exception", e);
        }
        break;

      case CONNECTION_MESSAGE:
        try {

          senderURI = wonMessage.getMessageEvent().getSenderURI();
          URI receiverURI = wonMessage.getMessageEvent().getReceiverURI();

          List<Connection> connections =
              connectionRepository.findByNeedURIAndRemoteNeedURI(senderURI, receiverURI);

          URI connectionURI = connections.get(0).getConnectionURI();

          content = wonMessage.getMessageEvent().getModel();

          ownerProtocolService.sendMessage(connectionURI, content, null);
        } catch (Exception e) {
          logger.warn("caught Exception", e);
        }
        break;

      default:
        break;
    }
  }

  public void handleMessageEventFromWonNode(Dataset wonMessage)
  {
    handleMessageEventFromWonNode(WonMessageDecoder.decodeFromDataset(wonMessage));
  }

  public void handleMessageEventFromWonNode(WonMessage wonMessage)
  {

    // ToDo (FS): handle messages

    ownerApplicationServiceCallbackToClient.onMessage(wonMessage);

  }

  // ToDo (FS): most (all?) of the response messages should be send back from the WON node (this is only temporary)
  private void sendBackResponseMessageToClient (WonMessage wonMessage)
  {

    URI responseMessageURI = null;

    responseMessageURI = URI.create("http://example.com/responseMessage/837ddj");//new URI(WONMSG.getGraphURI(msgURI.toString()).toString());

    WonMessageBuilder wonMessageBuilder = new WonMessageBuilder();
    WonMessage responseWonMessage = wonMessageBuilder
        .setWonMessageType(WonMessageType.CREATE_RESPONSE)
        .setMessageURI(responseMessageURI)
        .setSenderURI(wonMessage.getMessageEvent().getReceiverURI())
        .setReceiverURI(wonMessage.getMessageEvent().getSenderURI())
        .setWonMessageType(WonMessageType.CREATE_RESPONSE)
        .addRefersToURI(wonMessage.getMessageEvent().getMessageURI())
        .build();

    ownerApplicationServiceCallbackToClient.onMessage(responseWonMessage);
  }


  // ToDo (FS): methods only used until the messaging system is completely refactored then only one callback method will be used
  @Override
  public void onHint(final Match match, final Model content)
  {

    // since we have no message URI at this point we just generate one
    Random rand = new Random();
    URI messageURI = null;
    URI contentURI = null;
    try {
      messageURI = new URI(match.getOriginator().toString() + "/hintMessage/" + rand.nextInt());
      contentURI = new URI(match.getOriginator().toString() + "/hint/" + rand.nextInt());
    } catch (URISyntaxException e) {
      logger.warn("caught URISyntaxException:", e);
    }

    WonMessageBuilder wonMessageBuilder = new WonMessageBuilder();
    WonMessage wonMessage = wonMessageBuilder
        .setWonMessageType(WonMessageType.HINT_MESSAGE)
        .setMessageURI(messageURI)
        .setSenderURI(match.getOriginator())
        .setReceiverURI(match.getToNeed())
        .addContent(contentURI, content, null)
        .build();

    ownerApplicationServiceCallbackToClient.onMessage(wonMessage);
  }

  @Override
  public void onConnect(final Connection con, final Model content)
  {

    // since we have no message URI at this point we just generate one
    Random rand = new Random();
    URI messageURI = null;
    URI contentURI = null;
    try {
      messageURI = new URI(con.getRemoteConnectionURI().toString() + "/event/" + rand.nextInt());
      contentURI = new URI(con.getRemoteConnectionURI().toString() + "/eventContent/" + rand.nextInt());
    } catch (URISyntaxException e) {
      logger.warn("caught URISyntaxException:", e);
    }

    WonMessageBuilder wonMessageBuilder = new WonMessageBuilder();
    WonMessage wonMessage = wonMessageBuilder
        .setWonMessageType(WonMessageType.CONNECT)
        .setMessageURI(messageURI)
        .setReceiverURI(con.getNeedURI())
        .addContent(contentURI, content, null)
        .build();

    ownerApplicationServiceCallbackToClient.onMessage(wonMessage);
  }

  @Override
  public void onOpen(final Connection con, final Model content)
  {
    // since we have no message URI at this point we just generate one
    Random rand = new Random();
    URI messageURI = null;
    URI contentURI = null;
    try {
      messageURI = new URI(con.getRemoteConnectionURI().toString() + "/event/" + rand.nextInt());
      contentURI = new URI(con.getRemoteConnectionURI().toString() + "/eventContent/" + rand.nextInt());
    } catch (URISyntaxException e) {
      logger.warn("caught URISyntaxException:", e);
    }

    WonMessageBuilder wonMessageBuilder = new WonMessageBuilder();
    WonMessage wonMessage = wonMessageBuilder
        .setWonMessageType(WonMessageType.OPEN)
        .setMessageURI(messageURI)
        .setReceiverURI(con.getConnectionURI())
        .addContent(contentURI, content, null)
        .build();

    ownerApplicationServiceCallbackToClient.onMessage(wonMessage);
  }

  @Override
  public void onClose(final Connection con, final Model content)
  {
    // since we have no message URI at this point we just generate one
    Random rand = new Random();
    URI messageURI = null;
    URI contentURI = null;
    try {
      messageURI = new URI(con.getRemoteConnectionURI().toString() + "/event/" + rand.nextInt());
      contentURI = new URI(con.getRemoteConnectionURI().toString() + "/eventContent/" + rand.nextInt());
    } catch (URISyntaxException e) {
      logger.warn("caught URISyntaxException:", e);
    }

    WonMessageBuilder wonMessageBuilder = new WonMessageBuilder();
    WonMessage wonMessage = wonMessageBuilder
        .setWonMessageType(WonMessageType.CLOSE)
        .setMessageURI(messageURI)
        .setReceiverURI(con.getConnectionURI())
        .addContent(contentURI, content, null)
        .build();

    ownerApplicationServiceCallbackToClient.onMessage(wonMessage);
  }

  @Override
  public void onTextMessage(final Connection con, final ChatMessage message, final Model content)
  {
    // since we have no message URI at this point we just generate one
    Random rand = new Random();
    URI messageURI = null;
    URI contentURI = null;
    try {
      messageURI = new URI(con.getRemoteConnectionURI().toString() + "/event/" + rand.nextInt());
      contentURI = new URI(con.getRemoteConnectionURI().toString() + "/eventContent/" + rand.nextInt());
    } catch (URISyntaxException e) {
      logger.warn("caught URISyntaxException:", e);
    }

    WonMessageBuilder wonMessageBuilder = new WonMessageBuilder();
    WonMessage wonMessage = wonMessageBuilder
        .setWonMessageType(WonMessageType.CONNECTION_MESSAGE)
        .setMessageURI(messageURI)
        .setReceiverURI(con.getConnectionURI())
        .addContent(contentURI, content, null)
        .build();

    // ToDo (FS): if ChatMessage content is not in the content add ChatMessage to wonMessage

    ownerApplicationServiceCallbackToClient.onMessage(wonMessage);
  }

}
