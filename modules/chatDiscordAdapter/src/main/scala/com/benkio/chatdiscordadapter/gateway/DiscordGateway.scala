package com.benkio.chatdiscordadapter.gateway

import cats.effect.kernel.Resource
import cats.effect.std.Dispatcher
import cats.effect.Async
import cats.syntax.all.*
import log.effect.LogWriter
import net.dv8tion.jda.api.requests.GatewayIntent

/** The lifecycle of the gateway connection of a bot: the connection is opened when the resource is acquired and closed
  * when it is released. The reconnections after a network failure are done by the library.
  */
object DiscordGateway {

  /** Opens the connection, waits that it is ready, closes it on release: it asks to shut down, waits for the shutdown
    * timeout and then closes without waiting for the pending requests. The connection is closed also if it never gets
    * ready (e.g. the library fails while it connects). A failure while closing is logged, not raised.
    */
  def managed[F[_]: Async: LogWriter](
      open: F[GatewayClient[F]],
      shutdownTimeout: scala.concurrent.duration.FiniteDuration
  ): Resource[F, GatewayClient[F]] =
    Resource
      .make(LogWriter.info("[DiscordGateway] Connecting") >> open)(close(_, shutdownTimeout))
      .evalTap(_.awaitReady >> LogWriter.info("[DiscordGateway] Connected"))

  /** The connection with the library, to the gateway with the given token and intents. The listeners receive the events
    * of the library, see the [[net.dv8tion.jda.api.hooks.ListenerAdapter]].
    */
  def resource[F[_]: Async: LogWriter](
      token: String,
      config: DiscordGatewayConfig,
      listeners: List[AnyRef] = List.empty
  ): Resource[F, GatewayClient[F]] =
    Dispatcher.sequential[F].flatMap { dispatcher =>
      managed(JdaGatewayClient.open[F](dispatcher, token, config.intents, listeners), config.shutdownTimeout)
    }

  /** Connects, and completes when the connection is closed for good. */
  def run[F[_]: Async: LogWriter](
      token: String,
      config: DiscordGatewayConfig,
      listeners: List[AnyRef] = List.empty
  ): F[Unit] =
    resource[F](token = token, config = config, listeners = listeners).use(_.closed) >> LogWriter.info(
      "[DiscordGateway] Closed"
    )

  private def close[F[_]: Async: LogWriter](
      client: GatewayClient[F],
      timeout: scala.concurrent.duration.FiniteDuration
  ): F[Unit] =
    (LogWriter.info("[DiscordGateway] Shutting down") >>
      client.shutdown >>
      client
        .awaitShutdown(timeout)
        .flatMap(done =>
          Async[F].unlessA(done)(
            LogWriter.warn(s"[DiscordGateway] Still open after $timeout, closing without waiting") >>
              client.shutdownNow
          )
        )).handleErrorWith(e => LogWriter.error(s"[DiscordGateway] Error while closing: $e"))

  /** The intents the bot asks for: the messages of the servers and of the DMs. Whether the privileged message content
    * is needed depends on how the messages are read, see the inbound issue.
    */
  val defaultIntents: Set[GatewayIntent] = Set(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)
}
