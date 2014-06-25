package won.owner.protocol.impl;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import won.node.facet.impl.WON_TX;
import won.protocol.exception.NoSuchConnectionException;
import won.protocol.exception.NoSuchNeedException;
import won.protocol.model.*;
import won.protocol.owner.OwnerProtocolNeedServiceClientSide;
import won.protocol.repository.ChatMessageRepository;
import won.protocol.repository.ConnectionRepository;
import won.protocol.repository.FacetRepository;
import won.protocol.repository.NeedRepository;
import won.protocol.util.RdfUtils;
import won.protocol.util.WonRdfUtils;
import won.protocol.vocabulary.WON;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Implementation of the ownerProtocolNeedService to be used on the owner side. It contains the
 * required business logic to store state and delegates calls to an injected linked data
 * client and to an injected ownerProtocolNeedService implementation.
 * <p/>
 * User: Gabriel
 * Date: 03.12.12
 * Time: 14:42
 * TODO: refactor to separate communication code from business logic!
 */
public class OwnerProtocolNeedServiceClient implements OwnerProtocolNeedServiceClientSide
{
  /* Linked Data default paths */
  private static final String NEED_URI_PATH_PREFIX = "/data/need";
  private static final String CONNECTION_URI_PATH_PREFIX = "/data/connection";
  private static final String NEED_CONNECTION_URI_PATH_SUFFIX = "/connections";
  private static final String NEED_MATCH_URI_PATH_SUFFIX = "/matches";
  final Logger logger = LoggerFactory.getLogger(getClass());
  @Value(value = "${uri.node.default}")
  String wonNodeDefault;
  private ApplicationContext ownerApplicationContext;
  @Autowired
  private FacetRepository facetRepository;
  @Autowired
  private ChatMessageRepository chatMessageRepository;
  @Autowired
  private ConnectionRepository connectionRepository;
  @Autowired
  private NeedRepository needRepository;
  @Autowired
  private Executor executor;
  //ref=ownerProtocolNeedServiceClientJMSBased
  private OwnerProtocolNeedServiceClientSide delegate;

  @Override
  @Transactional(propagation = Propagation.SUPPORTS)
  public void activate(URI needURI) throws Exception {
    logger.debug("owner to need: ACTIVATE called for need {}", needURI);
    List<Need> needs = needRepository.findByNeedURI(needURI);
    if (needs.size() != 1)
      throw new NoSuchNeedException(needURI);
    delegate.activate(needURI);
    Need need = needs.get(0);
    need.setState(NeedState.ACTIVE);
    needRepository.save(need);
  }

  @Override
  @Transactional(propagation = Propagation.SUPPORTS)
  public void deactivate(URI needURI) throws Exception {
    logger.debug("owner to need: DEACTIVATE called for need {}", needURI);

    List<Need> needs = needRepository.findByNeedURI(needURI);
    if (needs.size() != 1)
      throw new NoSuchNeedException(needURI);
    delegate.deactivate(needURI);
    Need need = needs.get(0);
    need.setState(NeedState.INACTIVE);
    needRepository.save(need);

  }

  @Override
  @Transactional(propagation = Propagation.SUPPORTS)
  public void open(URI connectionURI, Model content) throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("owner to need: OPEN called for connection {} with content {}", connectionURI,
        StringUtils.abbreviate(RdfUtils.toString(content), 200));
    }
    List<Connection> cons = connectionRepository.findByConnectionURI(connectionURI);
    if (cons.size() != 1)
      throw new NoSuchConnectionException(connectionURI);

    delegate.open(connectionURI, content);

    Connection con = cons.get(0);
    con.setState(con.getState().transit(ConnectionEventType.OWNER_OPEN));
    connectionRepository.save(con);

  }

  @Override
  @Transactional(propagation = Propagation.SUPPORTS)
  public void close(final URI connectionURI, Model content) throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("owner to need: CLOSE called for connection {} with model {}", connectionURI,
        StringUtils.abbreviate(RdfUtils.toString(content), 200));
    }
    List<Connection> cons = connectionRepository.findByConnectionURI(connectionURI);
    if (cons.size() != 1)
      throw new NoSuchConnectionException(connectionURI);
    delegate.close(connectionURI, content);
    Connection con = cons.get(0);
    con.setState(con.getState().transit(ConnectionEventType.OWNER_CLOSE));
    connectionRepository.save(con);

  }

  @Override
  @Transactional(propagation = Propagation.SUPPORTS)
  public void textMessage(final URI connectionURI, final Model message)
    throws Exception {
    logger.debug("owner to need: MESSAGE called for connection {} with message {}", connectionURI, message);

    List<Connection> cons = connectionRepository.findByConnectionURI(connectionURI);
    if (cons.isEmpty())
      throw new NoSuchConnectionException(connectionURI);
    Connection con = cons.get(0);
    //todo: text message shall be returned
    delegate.textMessage(connectionURI, message);
    //todo: the parameter for setMessage method shall be set by retrieving the result of delegate.textMessage method
    ChatMessage chatMessage = new ChatMessage();
    chatMessage.setCreationDate(new Date());
    chatMessage.setLocalConnectionURI(connectionURI);

    Resource baseRes = message.getResource(message.getNsPrefixURI(""));
    StmtIterator stmtIterator = baseRes.listProperties(WON_TX.HAS_TEXT_MESSAGE);
    String textMessage = null;
    while (stmtIterator.hasNext()) {
      RDFNode obj = stmtIterator.nextStatement().getObject();
      if (obj.isLiteral()) {
        textMessage = obj.asLiteral().getLexicalForm();
        break;
      }
    }
    if (textMessage == null) {
      logger.debug("could not extract text message from RDF content of message");
      textMessage = "[could not extract text message]";
    }


    chatMessage.setMessage(textMessage);
    chatMessage.setOriginatorURI(con.getNeedURI());

    //save in the db
    chatMessageRepository.save(chatMessage);


  }

  @Override
  @Transactional(propagation = Propagation.SUPPORTS)
  public String register(URI endpointURI) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  @Transactional(propagation = Propagation.SUPPORTS)
  public ListenableFuture<URI> createNeed(URI ownerURI, Model content, boolean activate) throws Exception {
    return createNeed(ownerURI, content, activate, null);
  }

  @Override
  @Transactional(propagation = Propagation.SUPPORTS)
  public synchronized ListenableFuture<URI> createNeed(final URI ownerURI, final Model content, final boolean activate, final URI wonNodeUri)
    throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("owner to need: CREATE_NEED called for owner URI {}, activate {}, with content {}",
        new Object[]{ownerURI, activate, StringUtils.abbreviate(RdfUtils.toString(content), 200)});
    }
    final ListenableFuture<URI> uri = delegate.createNeed(ownerURI, content, activate, wonNodeUri);
    //asynchronously wait for the result and update the local database.
    //meanwhile, create our own ListenableFuture to pass the result back
    final SettableFuture<URI> result = SettableFuture.create();
    executor.execute(
      new Runnable()
      {
        @Override
        public void run() {
          //TODO: move the DB part into its own layer or something, because the owner webapp is designed to be syncronous, but the code below may slow down the web-app.
          Need need = new Need();
          try {
            need.setNeedURI(uri.get());
            // logger.debug(need.getNeedURI().toString());
            need.setState(activate ? NeedState.ACTIVE : NeedState.INACTIVE);
            need.setOwnerURI(ownerURI);

            if (wonNodeUri == null) need.setWonNodeURI(URI.create(wonNodeDefault));
            else need.setWonNodeURI(wonNodeUri);
            needRepository.save(need);

            ResIterator needIt = content.listSubjectsWithProperty(RDF.type, WON.NEED);
            if (!needIt.hasNext()) throw new IllegalArgumentException("at least one RDF node must be of type won:Need");

            Resource needRes = needIt.next();
            logger.debug("processing need resource {}", needRes.getURI());

            StmtIterator stmtIterator = content.listStatements(needRes, WON.HAS_FACET, (RDFNode) null);
            if (!stmtIterator.hasNext())
              throw new IllegalArgumentException("at least one RDF node must be of type won:HAS_FACET");
            else
              do {
                Facet facet = new Facet();
                facet.setNeedURI(need.getNeedURI());
                facet.setTypeURI(URI.create(stmtIterator.next().getObject().asResource().getURI()));
                facetRepository.save(facet);
              } while (stmtIterator.hasNext());
            //now that we're done, let our callers know the URI
            result.set(need.getNeedURI());
          } catch (Exception e) {
              logger.info("Error creating need {}. Stacktrace follows", need);
              logger.warn("Error creating need", e);
              result.cancel(true);
          }
        }
      }
    );
    return result;
  }

  @Override
  @Transactional(propagation = Propagation.SUPPORTS)
  public ListenableFuture<URI> connect(final URI needURI, final URI otherNeedURI, final Model content)
    throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("owner to need: CONNECT called for need {}, other need {} and content {}",
        new Object[]{needURI, otherNeedURI, StringUtils.abbreviate(RdfUtils.toString(content), 200)});
    }

    final ListenableFuture<URI> uri = delegate.connect(needURI, otherNeedURI, content);
    //asynchronously wait for the result and update the local database.
    //meanwhile, create our own ListenableFuture to pass the result back
    final SettableFuture<URI> result = SettableFuture.create();

    //now perform the connection state change or the creation of a new connection object
    //locally.
    this.executor.execute(new Runnable(){
      @Override
      public void run() {
        //find out the facet to connect with
        final URI facetURI = WonRdfUtils.FacetUtils.getFacet(content);
        //save the connection object in the database
        Connection con = null;
        try {
          //Create new connection object
          con = new Connection();
          con.setNeedURI(needURI);
          con.setState(ConnectionState.REQUEST_SENT);
          con.setTypeURI(facetURI);
          con.setRemoteNeedURI(otherNeedURI);
          con.setConnectionURI(uri.get());
          if (logger.isDebugEnabled()) {
            logger.debug("saving connection: {}", con);
          }
          connectionRepository.save(con);
          result.set(con.getConnectionURI());
        } catch (Exception e) {
          logger.info("Error creating connection {}. Stacktrace follows", con);
          logger.warn("Error creating connection ", e);
          result.cancel(true);
        }
      }
    });

    return result;
  }

  public void setDelegate(OwnerProtocolNeedServiceClientSide delegate) {
    this.delegate = delegate;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.ownerApplicationContext = applicationContext;
  }


}
