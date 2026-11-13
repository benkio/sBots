package com.benkio.chatircadapter.connection

import scala.concurrent.duration.FiniteDuration

/** The connection of a bot to the IRC server, as far as its lifecycle goes. It is the only thing [[IrcConnection]]
  * knows about the library ([[KittehConnection]]), so the lifecycle can be tested without a server.
  */
trait IrcClientConnection[F[_]] {

  /** Completes when the bot is connected and registered with the server. */
  def awaitReady: F[Unit]

  /** Completes when the connection is over for good: it was shut down. A connection that is lost and recovered by a
    * reconnection does not count.
    */
  def closed: F[Unit]

  /** Quits the server and stops reconnecting, without waiting. */
  def shutdown: F[Unit]

  /** Waits that the connection is closed: `false` if it is still open after the timeout. */
  def awaitShutdown(timeout: FiniteDuration): F[Boolean]
}
