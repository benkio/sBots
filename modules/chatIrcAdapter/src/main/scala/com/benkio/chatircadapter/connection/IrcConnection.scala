package com.benkio.chatircadapter.connection

import cats.effect.std.Dispatcher
import cats.effect.Async
import cats.effect.Resource
import cats.syntax.all.*
import log.effect.LogWriter

import scala.concurrent.duration.FiniteDuration

/** The lifecycle of the connection of a bot: it is opened when the resource is acquired and closed when it is released.
  * The reconnections after the connection is lost are done by the connection itself, see [[KittehConnection]].
  */
object IrcConnection {

  /** Opens the connection and waits that it is ready, up to the ready timeout. On release it quits and waits for the
    * shutdown timeout. The connection is closed also if it never gets ready. A failure while closing is logged, not
    * raised.
    */
  def managed[F[_]: Async: LogWriter, C <: IrcClientConnection[F]](
      open: F[C],
      readyTimeout: FiniteDuration,
      shutdownTimeout: FiniteDuration
  ): Resource[F, C] =
    Resource
      .make(LogWriter.info("[IrcConnection] Connecting") >> open)(close(_, shutdownTimeout))
      .evalTap(client =>
        Async[F].timeout(client.awaitReady, readyTimeout) >> LogWriter.info("[IrcConnection] Connected")
      )

  /** The connection with Kitteh, for the nick, with the optional password of its account (SASL). */
  def resource[F[_]: Async: LogWriter](
      nick: String,
      password: Option[String],
      config: IrcConfig,
      listeners: List[AnyRef] = List.empty
  ): Resource[F, KittehConnection[F]] =
    Dispatcher.sequential[F].flatMap { dispatcher =>
      managed(
        open = KittehConnection.open[F](dispatcher, nick, password, config, listeners),
        readyTimeout = config.readyTimeout,
        shutdownTimeout = config.shutdownTimeout
      )
    }

  /** Connects, and completes when the connection is closed for good. */
  def run[F[_]: Async: LogWriter](
      nick: String,
      password: Option[String],
      config: IrcConfig,
      listeners: List[AnyRef] = List.empty
  ): F[Unit] =
    resource[F](nick = nick, password = password, config = config, listeners = listeners).use(_.closed) >> LogWriter
      .info("[IrcConnection] Closed")

  private def close[F[_]: Async: LogWriter](client: IrcClientConnection[F], timeout: FiniteDuration): F[Unit] =
    (LogWriter.info("[IrcConnection] Quitting") >>
      client.shutdown >>
      client
        .awaitShutdown(timeout)
        .flatMap(done =>
          Async[F].unlessA(done)(LogWriter.warn(s"[IrcConnection] Still open after $timeout, giving up on waiting"))
        )).handleErrorWith(e => LogWriter.error(s"[IrcConnection] Error while closing: $e"))
}
