/*
 * Copyright 2012  Research Studios Austria Forschungsges.m.b.H.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package won.node.protocol.impl;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import won.node.protocol.MatcherProtocolMatcherServiceClientSide;
import won.node.service.impl.URIService;
import won.protocol.jms.MessagingService;
import won.protocol.message.WonMessage;
import won.protocol.message.WonMessageEncoder;
import won.protocol.model.Need;
import won.protocol.repository.ConnectionRepository;
import won.protocol.repository.NeedRepository;
import won.protocol.util.RdfUtils;

import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatcherProtocolMatcherClientImplJMSBased implements MatcherProtocolMatcherServiceClientSide
{
  final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private MessagingService messagingService;

  @Autowired
  private NeedRepository needRepository;

  @Autowired
  private ConnectionRepository connectionRepository;

  @Autowired
  private URIService uriService;

  @Override
  public void matcherRegistered(final URI wonNodeURI, final WonMessage wonMessage) {
    Map headerMap = new HashMap<String, String>();
    headerMap.put("wonNodeURI", wonNodeURI.toString());
    headerMap.put("wonMessage", WonMessageEncoder.encode(wonMessage, Lang.TRIG));
    headerMap.put("protocol","MatcherProtocol");
    headerMap.put("methodName", "matcherRegistered");
    messagingService.sendInOnlyMessage(null,headerMap,null,"outgoingMessages");
  }

  @Override
  public void needCreated(final URI needURI, final Model content, final WonMessage wonMessage)
  {

      StringWriter sw = new StringWriter();
      content.write(sw, "TTL");

      List<Need> needs = needRepository.findByNeedURI(needURI);
      Need need = needs.get(0);

      Map headerMap = new HashMap<String, String>();
      headerMap.put("needUri", needURI.toString());
      headerMap.put("content",RdfUtils.toString(content));
      headerMap.put("wonMessage", WonMessageEncoder.encode(wonMessage, Lang.TRIG));
      headerMap.put("protocol","MatcherProtocol");
      headerMap.put("methodName", "needCreated");
      headerMap.put("wonNodeURI", uriService.getGeneralURIPrefix()+"/resource");
      messagingService.sendInOnlyMessage(null,headerMap,null,"outgoingMessages");

  }
  @Override
  public void needActivated(final URI needURI, final WonMessage wonMessage){
    Map headerMap = new HashMap<String, String>();
    headerMap.put("needURI", needURI.toString());
    headerMap.put("wonMessage", WonMessageEncoder.encode(wonMessage, Lang.TRIG));
    headerMap.put("protocol","MatcherProtocol");
    headerMap.put("methodName","needActivated");
    headerMap.put("wonNodeURI", uriService.getGeneralURIPrefix()+"/resource");
    messagingService.sendInOnlyMessage(null, headerMap,null,"outgoingMessages");
  }
  @Override
  public void needDeactivated(final URI needURI, final WonMessage wonMessage){
    Map headerMap = new HashMap<String, String>();
    headerMap.put("needURI", needURI.toString());
    headerMap.put("wonMessage", WonMessageEncoder.encode(wonMessage, Lang.TRIG));
    headerMap.put("protocol","MatcherProtocol");
    headerMap.put("methodName","needDeactivated");
    headerMap.put("wonNodeURI", uriService.getGeneralURIPrefix()+"/resource");
    messagingService.sendInOnlyMessage(null, headerMap,null,"outgoingMessages");
  }


  public void setNeedRepository(NeedRepository needRepository) {
      this.needRepository = needRepository;
  }

  public void setMessagingService(MessagingService messagingService) {
      this.messagingService = messagingService;
  }

}