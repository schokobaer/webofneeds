package crawler.actor;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import akka.routing.FromConfig;
import crawler.config.Settings;
import crawler.config.SettingsImpl;
import crawler.exception.CrawlingWrapperException;
import crawler.message.UriStatusMessage;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.Map;

/**
 * Coordinates recursive crawling of linked data resources by assigning {@link crawler.message.UriStatusMessage}
 * to workers of type {@link crawler.actor.WorkerCrawlerActor} and one single worker of type
 * {@link UpdateMetadataActor}.
 * The process can be stopped at any time and continued by passing the messages that
 * should be recrawled again since meta data about the crawling process is saved
 * in the SPARQL endpoint. This is done by a single actor of type {@link UpdateMetadataActor}
 * which keeps message order to guarantee consistency in case of failure. Unfinished messages can
 * be resend for restarting crawling.
 *
 * User: hfriedrich
 * Date: 30.03.2015
 */
public class MasterCrawlerActor extends UntypedActor
{
  private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  private final SettingsImpl settings = Settings.SettingsProvider.get(getContext().system());
  private Map<String, UriStatusMessage> pendingMessages = null;
  private Map<String, UriStatusMessage> doneMessages = null;
  private Map<String, UriStatusMessage> failedMessages = null;
  private ActorRef crawlingWorker;
  private ActorRef updateMetaDataWorker;

  public MasterCrawlerActor() {
    pendingMessages = new HashMap<>();
    doneMessages = new HashMap<>();
    failedMessages = new HashMap<>();
  }

  @Override
  public void preStart() {

    // Create the router/pool with worker actors that do the actual crawling
    Props workerProps = Props.create(WorkerCrawlerActor.class);
    crawlingWorker = getContext().actorOf(new FromConfig().props(workerProps), "CrawlingRouter");

    // create a single meta data update actor for all worker actors
    updateMetaDataWorker = getContext().actorOf(Props.create(UpdateMetadataActor.class), "MetaDataUpdateWorker");
    getContext().watch(updateMetaDataWorker);
  }

  /**
   * set supervision strategy for worker actors and handle failed crawling actions
   *
   * @return
   */
  @Override
  public SupervisorStrategy supervisorStrategy() {

    SupervisorStrategy supervisorStrategy = new OneForOneStrategy(
      0, Duration.Zero(), new Function<Throwable, SupervisorStrategy.Directive>() {

      @Override
      public SupervisorStrategy.Directive apply(Throwable t) throws Exception {

        // save the failed status of a crawlingWorker during crawling
        if (t instanceof CrawlingWrapperException) {
          CrawlingWrapperException e = (CrawlingWrapperException) t;
          log.warning("Handled breaking message: {}", e.getBreakingMessage());
          log.warning("Exception was: {}", e.getException());
          process(e.getBreakingMessage());
          return SupervisorStrategy.resume();
        }

        // default behaviour in other cases
        return SupervisorStrategy.escalate();
      }
    });

    return supervisorStrategy;
  }

  /**
   * Process {@link crawler.message.UriStatusMessage} objects
   *
   * @param message
   */
  @Override
  public void onReceive(final Object message) {

    // if the update meta data worker terminated the master can be terminated too
    if (message instanceof Terminated) {
      if (((Terminated) message).getActor().equals(updateMetaDataWorker)) {
        log.info("Crawler shut down");
        getContext().system().shutdown();
      }
    }

    if (message instanceof UriStatusMessage) {
      UriStatusMessage uriMsg = (UriStatusMessage) message;
      process(uriMsg);
      log.debug("Number of pending messages: {}", pendingMessages.size());
    } else {
      unhandled(message);
    }
  }

  private void logStatus() {
    log.info("Number of URIs\n Crawled: {}\n Failed: {}\n Pending: {}",
             doneMessages.size(), failedMessages.size(), pendingMessages.size());
  }

  /**
   * Pass the messages to process to the workers and update meta data about crawling
   *
   * @param msg
   */
  private void process(UriStatusMessage msg) {

    log.debug("Process message: {}", msg);
    if (msg.getStatus().equals(UriStatusMessage.STATUS.PROCESS)) {

      // multiple extractions of the same URI can happen quite often since the extraction
      // query uses property path from base URI which may return URIs that are already
      // processed. So filter out these messages here
      if (pendingMessages.get(msg.getUri()) != null ||
        doneMessages.get(msg.getUri()) != null ||
        failedMessages.get(msg.getUri()) != null) {
        log.debug("message {} already processing/processed ...", msg);
        return;
      }

      // start crawling URI
      updateMetaDataWorker.tell(msg, getSelf());
      pendingMessages.put(msg.getUri(), msg);
      crawlingWorker.tell(msg, getSelf());

    } else if (msg.getStatus().equals(UriStatusMessage.STATUS.DONE)) {

      // URI crawled successfully
      log.debug("Successfully processed URI: {}", msg.getUri());
      updateMetaDataWorker.tell(msg, getSelf());
      pendingMessages.remove(msg.getUri());
      if (doneMessages.put(msg.getUri(), msg) != null) {
        log.warning("URI message received twice: {}", msg.getUri());
      }
      logStatus();

    } else if (msg.getStatus().equals(UriStatusMessage.STATUS.FAILED)) {

      // Crawling failed
      log.debug("Crawling URI failed: {}", msg.getUri());
      updateMetaDataWorker.tell(msg, getSelf());
      pendingMessages.remove(msg.getUri());
      failedMessages.put(msg.getUri(), msg);
      crawlingWorker.tell(msg, getSelf());
      logStatus();
    }

    // terminate
    if (pendingMessages.size() == 0) {
      log.info("Terminating crawler ...");
      updateMetaDataWorker.tell(PoisonPill.getInstance(), getSelf());
    }
  }


}