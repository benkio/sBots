package com.benkio.chatdiscordadapter.gateway

import cats.effect.std.Dispatcher
import cats.effect.Async
import cats.effect.Deferred
import cats.syntax.all.*
import log.effect.LogWriter
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent
import net.dv8tion.jda.api.events.session.SessionRecreateEvent
import net.dv8tion.jda.api.events.session.SessionResumeEvent
import net.dv8tion.jda.api.events.session.ShutdownEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder

import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters.*

/** The [[GatewayClient]] of JDA. JDA reconnects and resumes the session by itself (`autoReconnect`), here the
  * disconnections are only logged.
  */
object JdaGatewayClient {

  def open[F[_]: Async: LogWriter](
      dispatcher: Dispatcher[F],
      token: String,
      intents: Set[GatewayIntent],
      listeners: List[AnyRef]
  ): F[JdaGatewayClient[F]] =
    Deferred[F, Unit].flatMap { closedSignal =>
      val lifecycle = new ListenerAdapter {
        override def onSessionDisconnect(event: SessionDisconnectEvent): Unit =
          dispatcher.unsafeRunAndForget(LogWriter.warn("[DiscordGateway] Disconnected, JDA tries to reconnect"))
        override def onSessionResume(event: SessionResumeEvent): Unit =
          dispatcher.unsafeRunAndForget(LogWriter.info("[DiscordGateway] Session resumed"))
        override def onSessionRecreate(event: SessionRecreateEvent): Unit =
          dispatcher.unsafeRunAndForget(LogWriter.info("[DiscordGateway] Session recreated"))
        override def onShutdown(event: ShutdownEvent): Unit =
          dispatcher.unsafeRunAndForget(closedSignal.complete(()).void)
      }
      Async[F]
        .blocking(
          JDABuilder
            .createLight(token, intents.asJava)
            .setAutoReconnect(true)
            .addEventListeners((lifecycle :: listeners)*)
            .build()
        )
        .map(jda => new JdaGatewayClient[F](jda, closedSignal))
    }
}

/** The connection of JDA: the `jda` is what the adapter sends the replies with. */
class JdaGatewayClient[F[_]: Async](val jda: JDA, closedSignal: Deferred[F, Unit]) extends GatewayClient[F] {
  override def awaitReady: F[Unit]                                = Async[F].interruptible(jda.awaitReady()).void
  override def closed: F[Unit]                                    = closedSignal.get
  override def shutdown: F[Unit]                                  = Async[F].delay(jda.shutdown())
  override def shutdownNow: F[Unit]                               = Async[F].delay(jda.shutdownNow()).void
  override def awaitShutdown(timeout: FiniteDuration): F[Boolean] =
    Async[F].interruptible(jda.awaitShutdown(java.time.Duration.ofMillis(timeout.toMillis)))
}
