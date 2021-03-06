package won.node.service.impl;

import org.apache.jena.graph.TripleBoundary;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import won.protocol.exception.*;
import won.protocol.model.*;
import won.protocol.repository.*;
import won.protocol.service.WonNodeInformationService;
import won.protocol.util.DataAccessUtils;
import won.protocol.vocabulary.WON;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * User: gabriel
 * Date: 06/11/13
 */
public class DataAccessServiceImpl implements won.node.service.DataAccessService
{
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private URIService URIService;

  @Autowired
  private NeedRepository needRepository;
  @Autowired
  private ConnectionRepository connectionRepository;
  @Autowired
  private FacetRepository facetRepository;
  @Autowired
  private WonNodeInformationService wonNodeInformationService;
  @Autowired
  protected ConnectionContainerRepository connectionContainerRepository;
  @Autowired
  protected NeedEventContainerRepository needEventContainerRepository;
  @Autowired
  protected ConnectionEventContainerRepository connectionEventContainerRepository;
  @Autowired
  protected DatasetHolderRepository datasetHolderRepository;



  /**
   * Creates a new Connection object.
   *
   * @param connectionURI
   * @param needURI
   * @param otherNeedURI
   * @param otherConnectionURI
   * @param facetURI
   * @param connectionState
   * @param connectionEventType
   * @return
   * @throws NoSuchNeedException
   * @throws IllegalMessageForNeedStateException
   * @throws ConnectionAlreadyExistsException
   */
  public Connection createConnection(final URI connectionURI, final URI needURI, final URI otherNeedURI, final URI otherConnectionURI,
                                     final URI facetURI, final ConnectionState connectionState,
                                     final ConnectionEventType connectionEventType)
      throws NoSuchNeedException, IllegalMessageForNeedStateException, ConnectionAlreadyExistsException {

    if (needURI == null) throw new IllegalArgumentException("needURI is not set");
    if (otherNeedURI == null) throw new IllegalArgumentException("otherNeedURI is not set");
    if (needURI.equals(otherNeedURI)) throw new IllegalArgumentException("needURI and otherNeedURI are the same");
    if (facetURI == null) throw new IllegalArgumentException("facetURI is not set");

    //Load need (throws exception if not found)
    Need need = DataAccessUtils.loadNeed(needRepository, needURI);
    if (!isNeedActive(need))
      throw new IllegalMessageForNeedStateException(needURI, connectionEventType.name(), need.getState());

    //TODO: create a proper exception if a facet is not supported by a need
    if(facetRepository.findByNeedURIAndTypeURI(needURI, facetURI).isEmpty()) throw new RuntimeException("Facet '" + facetURI +"' is not supported by Need: '" + needURI + "'");
  /* Create connection */
    Connection con = new Connection();
    //create and set new uri
    con.setConnectionURI(connectionURI);
    con.setNeedURI(needURI);
    con.setState(connectionState);
    con.setRemoteNeedURI(otherNeedURI);
    con.setRemoteConnectionURI(otherConnectionURI);
    con.setTypeURI(facetURI);
    ConnectionEventContainer connectionEventContainer = new ConnectionEventContainer(con, connectionURI);
    try {
      con = connectionRepository.save(con);
      connectionEventContainerRepository.save(connectionEventContainer);
    } catch (Exception e){
      //we assume the unique key constraint on needURI, remoteNeedURI, typeURI was violated: we have to perform an
      // update, not an insert
      logger.warn("caught exception, assuming unique key constraint on needURI, remoteNeedURI, typeURI was violated" +
                    ". Throwing a ConnectionAlreadyExistsException. TODO: think about handling this exception " +
                    "separately", e);
      throw new ConnectionAlreadyExistsException(con.getConnectionURI(),needURI, otherNeedURI);
    }
    return con;
  }

  @Override
  public Collection<URI> getSupportedFacets(URI needUri) throws NoSuchNeedException
  {
    List<URI> ret = new LinkedList<URI>();
    Need need = DataAccessUtils.loadNeed(needRepository, needUri);
    DatasetHolder datasetHolder = need.getDatatsetHolder();
    Model content = null;
    content = datasetHolder.getDataset().getDefaultModel();
    if (content == null) {
      throw new IllegalStateException("tried to access content dataset of need '"+need.getNeedURI()+"' but found " +
                                        "none!");
    }
    Resource baseRes = content.getResource(content.getNsPrefixURI(""));
    StmtIterator stmtIterator = baseRes.listProperties(WON.HAS_FACET);
    while (stmtIterator.hasNext()) {
      RDFNode object = stmtIterator.nextStatement().getObject();
      if (object.isURIResource()){
        ret.add(URI.create(object.toString()));
      }
    }
    return ret;
  }


  @Override
  public Connection getConnection(List<Connection> connections, URI facetURI, ConnectionEventType eventType)
      throws ConnectionAlreadyExistsException {
    Connection con = null;
    for(Connection c : connections) {
      //TODO: check remote need type as well
      if (facetURI.equals(c.getTypeURI()))
        con = c;
    }
    return con;
  }


  @Override
  public Connection nextConnectionState(URI connectionURI, ConnectionEventType connectionEventType)
      throws NoSuchConnectionException, IllegalMessageForConnectionStateException {
    if (connectionURI == null) throw new IllegalArgumentException("connectionURI is not set");
    //load connection, checking if it exists
    Connection con = DataAccessUtils.loadConnection(connectionRepository, connectionURI);
    //perform state transit
    ConnectionState nextState = performStateTransit(con, connectionEventType);
    //set new state and save in the db
    con.setState(nextState);
    //save in the db
    return connectionRepository.save(con);
  }

  @Override
  public Connection nextConnectionState(Connection con, ConnectionEventType connectionEventType)
    throws IllegalMessageForConnectionStateException {
    //perform state transit
    ConnectionState nextState = performStateTransit(con, connectionEventType);
    //set new state and save in the db
    con.setState(nextState);
    //save in the db
    return connectionRepository.save(con);
  }

  /**
   * Adds feedback, represented by the subgraph reachable from feedback, to the RDF description of the
   * item identified by forResource
   * @param connection
   * @param feedback
   * @return true if feedback could be added false otherwise
   */
  @Override
  public boolean addFeedback(final Connection connection, final Resource feedback){
    //TODO: concurrent modifications to the model for this resource result in side-effects.
    //think about locking.
    logger.debug("adding feedback to resource {}", connection);
    DatasetHolder datasetHolder = connection.getDatasetHolder();
    Dataset dataset = null;
    if (datasetHolder == null) {
      //if no dataset is found, we create one.
      dataset = DatasetFactory.create();
      datasetHolder = new DatasetHolder(connection.getConnectionURI(), dataset);
      connection.setDatasetHolder(datasetHolder);
    } else {
      dataset = datasetHolder.getDataset();
    }
    Model model = dataset.getDefaultModel();
    Resource mainRes = model.getResource(connection.getConnectionURI().toString());
    if (mainRes == null){
      logger.debug("could not add feedback to resource {}: resource not found/created in model", connection.getConnectionURI());
      return false;
    }
    mainRes.addProperty(WON.HAS_FEEDBACK_EVENT, feedback);
    ModelExtract extract = new ModelExtract(new StatementTripleBoundary(TripleBoundary.stopNowhere));
    model.add(extract.extract(feedback, feedback.getModel()));
    dataset.setDefaultModel(model);
    datasetHolder.setDataset(dataset);
    datasetHolderRepository.save(datasetHolder);
    connectionRepository.save(connection);
    logger.debug("done adding feedback for resource {}", connection);
    return true;
  }



  @Override
  public void updateRemoteConnectionURI(Connection con, URI remoteConnectionURI) {
    if (logger.isDebugEnabled()) {
      logger.debug("updating remote connection URI of con {} to {}", con, remoteConnectionURI);
    }
    con.setRemoteConnectionURI(remoteConnectionURI);
    connectionRepository.save(con);
  }

  private boolean isNeedActive(final Need need) {
    return NeedState.ACTIVE == need.getState();
  }

  /**
   * Calculates the ATConnectionState resulting from the message in the current connection state.
   * Checks if the specified message is allowed in the connection's state and throws an exception if not.
   *
   * @param con
   * @param msg
   * @return
   * @throws won.protocol.exception.IllegalMessageForConnectionStateException
   *          if the message is not allowed in the connection's current state
   */
  private ConnectionState performStateTransit(Connection con, ConnectionEventType msg) throws IllegalMessageForConnectionStateException
  {
    if (!msg.isMessageAllowed(con.getState())) {
      throw new IllegalMessageForConnectionStateException(con.getConnectionURI(), msg.name(), con.getState());
    }
    return con.getState().transit(msg);
  }

  @Override
  public void setURIService(URIService URIService) {
    this.URIService = URIService;
  }



}
