package com.benkio.chatircadapter.connection

import pureconfig.ConfigReader

import scala.concurrent.duration.FiniteDuration

/** What is IRC specific in the configuration (`main.irc`): the server every IRC bot connects to, with a connection per
  * bot, as the nick is the identity on IRC.
  *
  * @param tls
  *   whether the connection is encrypted; the port of the server has to be the TLS one (usually 6697)
  * @param channels
  *   the channels every bot joins once connected
  * @param readyTimeout
  *   how long a bot is given to connect and register before it is given up (a wrong host does not fail by itself, the
  *   connection is retried)
  * @param shutdownTimeout
  *   how long the connection is given to close after the quit message
  * @param reconnectBaseDelay
  *   the delay before the first reconnection after the connection is lost, doubled at every failed attempt
  * @param reconnectMaxDelay
  *   the longest delay between two attempts
  */
final case class IrcConfig(
    host: String,
    port: Int,
    tls: Boolean,
    channels: List[String],
    readyTimeout: FiniteDuration,
    shutdownTimeout: FiniteDuration,
    reconnectBaseDelay: FiniteDuration,
    reconnectMaxDelay: FiniteDuration
) derives ConfigReader {

  /** The delay before the reconnection after `failedAttempts` attempts that failed: it doubles up to the maximum. */
  def reconnectDelay(failedAttempts: Int): FiniteDuration = {
    val doublings = math.min(failedAttempts.max(0), 30)
    val delay     = reconnectBaseDelay * (1L << doublings)
    if delay > reconnectMaxDelay then reconnectMaxDelay else delay
  }
}
