/*
 * Copyright 2012  Research Studios Austria Forschungsges.m.b.H.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package won.owner.messaging;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.riot.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import won.protocol.jms.MessagingService;
import won.protocol.message.WonMessage;
import won.protocol.message.WonMessageEncoder;
import won.protocol.message.processor.impl.KeyForNewNeedAddingProcessor;
import won.protocol.message.processor.impl.SignatureAddingWonMessageProcessor;
import won.protocol.message.sender.WonMessageSender;
import won.protocol.model.WonNode;
import won.protocol.repository.WonNodeRepository;
import won.protocol.util.RdfUtils;

/**
 * User: LEIH-NB
 * Date: 17.10.13
 *
 * Instance of this class receives events upon which it tries to register at the default won node using JMS.
 */
public class OwnerWonMessageSenderJMSBased implements ApplicationListener<WonNodeRegistrationEvent>, WonMessageSender
{

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private boolean isDefaultWonNodeRegistered = false;
  private MessagingService messagingService;
  private URI defaultNodeURI;

  //todo: make this configurable
  private String startingEndpoint;


  @Autowired
  private OwnerProtocolCommunicationServiceImpl ownerProtocolCommunicationServiceImpl;


  @Autowired
  private WonNodeRepository wonNodeRepository;

  @Autowired
  private SignatureAddingWonMessageProcessor signatureAddingProcessor ;

  @Autowired
  private KeyForNewNeedAddingProcessor needKeyGeneratorAndAdder;

  public void sendWonMessage(WonMessage wonMessage) {
    try {

      // TODO check if there is a better place for applying signing logic
      wonMessage = doSigningOnOwner(wonMessage);

      if (logger.isDebugEnabled()){
        logger.debug("sending this message: {}", RdfUtils.writeDatasetToString(wonMessage.getCompleteDataset(), Lang.TRIG));
      }

      // ToDo (FS): change it to won node URI and create method in the MessageEvent class
      URI wonNodeUri = wonMessage.getSenderNodeURI();

      if (wonNodeUri == null){
        //obtain the sender won node from the sender need
          throw new IllegalStateException("a message needs a SenderNodeUri otherwise we can't determine the won node " +
                                            "via which to send it");
      }

      String ownerApplicationId = getOwnerApplicationIdAndRegisterIfNecessary(wonNodeUri);


      //String ep = camelConfiguration.getEndpoint()
      String ep = ownerProtocolCommunicationServiceImpl.getProtocolCamelConfigurator()
                                           .getEndpoint(wonNodeUri);
      Map<String, Object> headerMap = new HashMap<>();
      headerMap.put("ownerApplicationID", ownerApplicationId);
      headerMap.put("remoteBrokerEndpoint",ep);
      messagingService
              .sendInOnlyMessage(null, headerMap, WonMessageEncoder.encode(wonMessage, Lang.TRIG), startingEndpoint);

      //camelContext.getShutdownStrategy().setSuppressLoggingOnTimeout(true);
    } catch (Exception e){
      throw new RuntimeException("could not send message", e);
    }
  }

  private String getOwnerApplicationIdAndRegisterIfNecessary(final URI wonNodeUri) throws Exception {
    ownerProtocolCommunicationServiceImpl.register(wonNodeUri, messagingService);
    List<WonNode> wonNodeList = wonNodeRepository.findByWonNodeURI(wonNodeUri);
    String ownerApplicationId = wonNodeList.get(0).getOwnerApplicationID();
    return ownerApplicationId;
  }

  //TODO: adding public keys and signing can be removed when it happens in the browser
  //in that case owner will have to sign only system messages, or in case it adds information to the message
  //TODO exceptions
  private WonMessage doSigningOnOwner(final WonMessage wonMessage)
    throws Exception {
    // add public key of the newly created need
    WonMessage outMessage = needKeyGeneratorAndAdder.process(wonMessage);
    // add signature:
    return signatureAddingProcessor.processOnBehalfOfNeed(outMessage);
  }

  /**
   * The owner application calls the register() method node upon initalization (and during fixed time intervals)
   * to connect to the default won node.
   *
   * @param wonNodeRegistrationEvent
   */
  @Override
  public void onApplicationEvent(final WonNodeRegistrationEvent wonNodeRegistrationEvent) {

    if (!isDefaultWonNodeRegistered) {
      try {
        new Thread()
        {
          @Override
          public void run() {
            try {

              logger.info("register at default won node {}", defaultNodeURI);
              ownerProtocolCommunicationServiceImpl.register(defaultNodeURI, messagingService);

              // try the registration as long as no exception occurs
              logger.info("successfully registered at default won node {}", defaultNodeURI);
              isDefaultWonNodeRegistered = true;

            } catch (Exception e) {
              logger.warn("Could not register with default won node {}. Try again later ... (reason is logged at " +
                            "loglevel 'DEBUG')",
                          defaultNodeURI);
              logger.debug("Exceptions is: ", e);
            }
          }
        }.start();

      } catch (Exception e) {
        logger.warn("Could not register with default won node {}. Try again later ...", defaultNodeURI);
        logger.debug("Exceptions is: ", e);
      }
    }
  }

  public void setMessagingService(MessagingService messagingService) {
    this.messagingService = messagingService;
  }

  public void setDefaultNodeURI(URI defaultNodeURI) {
    this.defaultNodeURI = defaultNodeURI;
  }

  public void setStartingEndpoint(String startingEndpoint) {
    this.startingEndpoint = startingEndpoint;
  }

}
