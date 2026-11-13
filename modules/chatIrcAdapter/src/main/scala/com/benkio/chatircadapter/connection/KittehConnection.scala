package com.benkio.chatircadapter.connection

import cats.effect.std.Dispatcher
import cats.effect.Async
import cats.effect.Deferred
import cats.syntax.all.*
import log.effect.LogWriter
import net.engio.mbassy.listener.Handler
import org.kitteh.irc.client.library.event.client.ClientNegotiationCompleteEvent
import org.kitteh.irc.client.library.event.connection.ClientConnectionEndedEvent
import org.kitteh.irc.client.library.feature.auth.SaslPlain
import org.kitteh.irc.client.library.feature.sending.SingleDelaySender
import org.kitteh.irc.client.library.Client
import org.kitteh.irc.client.library.Client.Builder.Server.SecurityType

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration.FiniteDuration

/** The [[IrcClientConnection]] of Kitteh. `client` is what the adapter sends the replies with. */
class KittehConnection[F[_]: Async](
    val client: Client,
    ready: Deferred[F, Unit],
    closedSignal: Deferred[F, Unit],
    shuttingDown: AtomicBoolean
) extends IrcClientConnection[F] {

  override def awaitReady: F[Unit] = ready.get
  override def closed: F[Unit]     = closedSignal.get
  override def shutdown: F[Unit]   = Async[F].delay {
    shuttingDown.set(true)
    client.shutdown("Bye")
  }
  override def awaitShutdown(timeout: FiniteDuration): F[Boolean] =
    Async[F].timeoutTo(closedSignal.get.as(true), timeout, Async[F].pure(false))
}

object KittehConnection {

  /** Connects to the server. When the connection is lost Kitteh reconnects after the delay this sets, which grows with
    * the failed attempts ([[IrcConfig.reconnectDelay]]) and starts again once the bot is registered. The channels are
    * joined at every registration, the first and the ones after a reconnection. After the shutdown nothing is
    * reconnected.
    *
    * @param password
    *   the password of the account of the nick: the bot identifies with SASL PLAIN
    * @param listeners
    *   the objects with the `@Handler` methods of Kitteh that receive its events
    */
  def open[F[_]: Async: LogWriter](
      dispatcher: Dispatcher[F],
      nick: String,
      password: Option[String],
      config: IrcConfig,
      listeners: List[AnyRef]
  ): F[KittehConnection[F]] =
    for {
      ready        <- Deferred[F, Unit]
      closedSignal <- Deferred[F, Unit]
      shuttingDown = new AtomicBoolean(false)
      lifecycle    = new Lifecycle[F](dispatcher, config, ready, closedSignal, shuttingDown)
      connection <- Async[F].blocking {
        val client = Client
          .builder()
          .nick(nick)
          .user(nick)
          .realName(nick)
          .server()
          .host(config.host)
          .port(config.port, if config.tls then SecurityType.SECURE else SecurityType.INSECURE)
          .`then`()
          .management()
          .messageSendingQueueSupplier(SingleDelaySender.getSupplier(config.messageDelay.toMillis.toInt))
          .`then`()
          .build()
        client.getEventManager.registerEventListener(lifecycle)
        listeners.foreach(client.getEventManager.registerEventListener(_))
        password.foreach(p => client.getAuthManager.addProtocol(new SaslPlain(client, nick, p)))
        client.connect()
        new KittehConnection[F](client, ready, closedSignal, shuttingDown)
      }
    } yield connection

  /** The events of the connection that decide its lifecycle. Kitteh calls them on its own thread. */
  private class Lifecycle[F[_]: Async](
      dispatcher: Dispatcher[F],
      config: IrcConfig,
      ready: Deferred[F, Unit],
      closedSignal: Deferred[F, Unit],
      shuttingDown: AtomicBoolean
  )(using log: LogWriter[F]) {
    private val failedAttempts = new AtomicInteger(0)

    @Handler
    def onRegistered(event: ClientNegotiationCompleteEvent): Unit = {
      failedAttempts.set(0)
      if config.channels.nonEmpty then event.getClient.addChannel(config.channels*)
      dispatcher.unsafeRunAndForget(log.info("[IrcConnection] Registered") >> ready.complete(()).void)
    }

    @Handler
    def onEnded(event: ClientConnectionEndedEvent): Unit =
      if shuttingDown.get() || !event.canAttemptReconnect() then {
        event.setAttemptReconnect(false)
        dispatcher.unsafeRunAndForget(closedSignal.complete(()).void)
      } else {
        val delay = config.reconnectDelay(failedAttempts.getAndIncrement())
        event.setAttemptReconnect(true)
        event.setReconnectionDelay(delay.toMillis.toInt)
        dispatcher.unsafeRunAndForget(
          log.warn(
            s"[IrcConnection] Connection lost (${event.getCause.map(_.getMessage).orElse("no cause")}), reconnecting in $delay"
          )
        )
      }
  }
}
