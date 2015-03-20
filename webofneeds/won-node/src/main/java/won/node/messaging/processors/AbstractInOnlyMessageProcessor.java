package won.node.messaging.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.jena.riot.Lang;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import won.cryptography.service.RandomNumberService;
import won.node.protocol.MatcherProtocolMatcherServiceClientSide;
import won.node.service.DataAccessService;
import won.protocol.jms.CamelConfiguration;
import won.protocol.jms.MessagingService;
import won.protocol.jms.NeedProtocolCommunicationService;
import won.protocol.message.WonMessage;
import won.protocol.model.Need;
import won.protocol.model.OwnerApplication;
import won.protocol.repository.*;
import won.protocol.repository.rdfstorage.RDFStorageService;
import won.protocol.service.LinkedDataService;
import won.protocol.service.NeedManagementService;
import won.protocol.service.WonNodeInformationService;
import won.protocol.util.RdfUtils;
import won.protocol.util.linkeddata.LinkedDataSource;

import java.net.URI;
import java.util.*;

/**
 * User: syim
 * Date: 02.03.2015
 */
public abstract class AbstractInOnlyMessageProcessor implements Processor
{

  protected Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

  public static Lang RDF_LANGUAGE_FOR_MESSAGE = Lang.TRIG;

  @Autowired
  protected MessagingService messagingService;
  @Autowired
  protected DataAccessService dataService;
  @Autowired
  protected RDFStorageService rdfStorage;
  @Autowired
  protected NeedManagementService needManagementService;
  @Autowired
  protected NeedRepository needRepository;
  @Autowired
  protected ConnectionRepository connectionRepository;
  @Autowired
  protected FacetRepository facetRepository;
  @Autowired
  protected OwnerApplicationRepository ownerApplicationRepository;
  @Autowired
  protected MessageEventRepository messageEventRepository;
  @Autowired
  protected LinkedDataService linkedDataService;
  @Autowired
  protected WonNodeInformationService wonNodeInformationService;
  @Autowired
  protected LinkedDataSource linkedDataSource;
  @Autowired
  protected MatcherProtocolMatcherServiceClientSide matcherProtocolMatcherClient;
  @Autowired
  protected RandomNumberService randomNumberService;
  @Autowired
  protected NeedProtocolCommunicationService needProtocolCommunicationService;

  public abstract void process(final Exchange exchange) throws Exception;


  protected void sendMessageToOwner(WonMessage message, URI needURI){
    Need need = needRepository.findOneByNeedURI(needURI);
    List<OwnerApplication> ownerApplications = need.getAuthorizedApplications();
    Map headerMap = new HashMap<String, Object>();
    headerMap.put("ownerApplications", toStringIds(ownerApplications));
    messagingService.sendInOnlyMessage(null, headerMap, RdfUtils.writeDatasetToString(message.getCompleteDataset(),
        RDF_LANGUAGE_FOR_MESSAGE),
                                       "outgoingMessages");
  }

  protected void sendMessageToOwner(WonMessage message, List<String> ownerApplicationIds){
    Map headerMap = new HashMap<String, Object>();
    headerMap.put("protocol","OwnerProtocol");
    headerMap.put("ownerApplications", ownerApplicationIds);
    messagingService.sendInOnlyMessage(null, headerMap, RdfUtils.writeDatasetToString(message.getCompleteDataset(),RDF_LANGUAGE_FOR_MESSAGE),
                                       "outgoingMessages");
  }

  protected void sendMessageToOwner(WonMessage message, String... ownerApplicationIds){
    Map headerMap = new HashMap<String, Object>();
    headerMap.put("ownerApplications", Arrays.asList(ownerApplicationIds));
    messagingService.sendInOnlyMessage(null, headerMap, RdfUtils.writeDatasetToString(message.getCompleteDataset(),RDF_LANGUAGE_FOR_MESSAGE),
                                       "outgoingMessages");
  }

  protected void sendMessageToNode(WonMessage message, URI needUri, URI remoteNeedUri){

    CamelConfiguration camelConfiguration = null;
    try {
      camelConfiguration = needProtocolCommunicationService.configureCamelEndpoint(remoteNeedUri,needUri,
                                                                                                      "seda:NeedProtocol.Out");
    } catch (Exception e) {
      logger.info("error sending message to node", e);
      throw new RuntimeException("error sending message to node: could not configure camel endpoint", e);
    }
    Map headerMap = new HashMap<String, Object>();
    headerMap.put("remoteBrokerEndpoint", camelConfiguration.getEndpoint());
    messagingService.sendInOnlyMessage(null, headerMap, RdfUtils.writeDatasetToString(message.getCompleteDataset(),RDF_LANGUAGE_FOR_MESSAGE),
                                       "outgoingMessages");
    throw new UnsupportedOperationException("not implemented!");
  }

  protected List<String> toStringIds(final List<OwnerApplication> ownerApplications) {
    List<String> ownerApplicationIds = new ArrayList<String>(ownerApplications.size());
    for(OwnerApplication app: ownerApplications){
      ownerApplicationIds.add(app.getOwnerApplicationId());
    }
    return ownerApplicationIds;
  }
}
