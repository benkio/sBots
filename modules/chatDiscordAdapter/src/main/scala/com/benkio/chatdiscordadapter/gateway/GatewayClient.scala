package com.benkio.chatdiscordadapter.gateway

import scala.concurrent.duration.FiniteDuration

/** The connection of a bot to the Discord gateway, as far as its lifecycle goes. It is the only thing
  * [[DiscordGateway]] knows about the library ([[JdaGatewayClient]]), so the lifecycle can be tested without Discord.
  */
trait GatewayClient[F[_]] {

  /** Completes when the connection is established and the library has its data. */
  def awaitReady: F[Unit]

  /** Completes when the connection is over for good: it was shut down. The disconnections that the library recovers
    * from, with a reconnect or a resume, do not count.
    */
  def closed: F[Unit]

  /** Asks to close the connection, without waiting. */
  def shutdown: F[Unit]

  /** Waits that the connection is closed: `false` if it is still open after the timeout. */
  def awaitShutdown(timeout: FiniteDuration): F[Boolean]

  /** Closes the connection without letting the pending requests finish. */
  def shutdownNow: F[Unit]
}
