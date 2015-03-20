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

package won.bot.framework.bot;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import won.protocol.message.WonMessage;
import won.protocol.model.Connection;
import won.protocol.model.Match;

import java.net.URI;

/**
 * A bot that manipulates needs.
 *
 * Note: Methods may throw runtime exceptions, which will be handled by the execution framework.
 */
public interface Bot
{
  public boolean knowsNeedURI(URI needURI);

  public boolean knowsNodeURI(URI wonNodeURI);

  public void onConnectFromOtherNeed(Connection con, final WonMessage wonMessage) throws Exception;
  public void onOpenFromOtherNeed(Connection con, final WonMessage wonMessage) throws Exception;
  public void onCloseFromOtherNeed(Connection con, final WonMessage wonMessage) throws Exception;
  public void onHintFromMatcher(Match match, final WonMessage wonMessage) throws Exception;
  public void onMessageFromOtherNeed(Connection con, final WonMessage wonMessage) throws Exception;
  public void onMatcherRegistered(URI wonNodeUri);
  public void onNewNeedCreatedNotificationForMatcher(final URI wonNodeURI, final URI needURI, final Dataset needModel);
  public void onNeedActivatedNotificationForMatcher(final URI wonNodeURI, final URI needURI);
  public void onNeedDeactivatedNotificationForMatcher(final URI wonNodeURI, final URI needURI);

  /**
   * Called when a message is received that indicates some error during processing of
   * a message previously sent by the bot.
   * @param failedMessageUri
   * @param wonMessage
   */
  public void onFailureMessage(URI failedMessageUri, WonMessage wonMessage);
  /**
   * Called when a message is received that indicates successful processing of
   * a message previously sent by the bot.
   * @param successfulMessageUri
   * @param wonMessage
   */
  public void onSuccessMessage(URI successfulMessageUri, WonMessage wonMessage);

  /**
   * Override this to be informed whenever the bot has created a new need successfully.
   * @param needUri
   * @param needModel
   */
  public void onNewNeedCreated(final URI needUri, final URI wonNodeUri, final Model needModel) throws Exception;

  /**
   * Init method, called exactly once by the framework before any other method is invoked.
   * The callee must make sure this call is thread-safe, e.g. by explicit synchronizing.
   */
  public void initialize() throws Exception;
  /**
   * Called by the framework to execute non-reactive tasks.
   * The callee must make sure this call is thread-safe, but explicit synchronization is strongly discouraged.
   */
  public void act() throws Exception;

  /**
   * Shutdown method called exactly once by the framework to allow the bot to free resources.
   * The callee must make sure this call is thread-safe, e.g. by explicit synchronizing.
   */
  public void shutdown() throws Exception;

  /**
   * The lifecycle phase the bot is currently in.
   * @return
   */
  public BotLifecyclePhase getLifecyclePhase();

  /**
   * Indicates whether the bot considers its work done. If true, the bot is ok with not receiving
   * incoming messages and not having its act() method called.
   *
   * @return
   */
  public boolean isWorkDone();

}
