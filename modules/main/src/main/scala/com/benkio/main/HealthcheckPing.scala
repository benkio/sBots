package com.benkio.main

import cats.effect.implicits.*
import cats.effect.Async
import cats.implicits.*
import com.benkio.chatcore.repository.db.DBLog
import cron4s.CronExpr
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.Stream
import log.effect.LogWriter
import org.http4s.*
import org.http4s.client.Client

import scala.concurrent.duration.*

/** The pings of the Healthchecks.io check: `<url>/start` on boot, `<url>` every cron tick, `<url>/fail` on a crash. A
  * ping that cannot be sent is only logged: the health report never takes the application down.
  */
object HealthcheckPing {

  /** How long to wait before the heartbeat is started again after an error. */
  val restartDelay: FiniteDuration = 30.seconds

  /** Signals the boot and starts the heartbeat in the background: it is started again after an error. */
  def healthcheckPing[F[_]: Async](
      dbLog: DBLog[F],
      client: Client[F],
      healthcheckEndpoint: Uri,
      healthcheckCron: CronExpr
  )(using log: LogWriter[F]): F[Unit] = {
    val ticks = Cron4sScheduler.systemDefault[F].awakeEvery(healthcheckCron).void
    val ping  = sendHealthcheckEndpoint(dbLog = dbLog, client = client, healthcheckEndpoint = healthcheckEndpoint).void
    start(client, healthcheckEndpoint) >>
      resilient(ticks = ticks, ping = ping, restartDelay = restartDelay).compile.drain.start.void
  }

  /** One ping per tick. An error, of the ping or of the ticks, is logged and the stream starts again after the delay,
    * so a transient error does not end the heartbeat.
    */
  private[main] def resilient[F[_]: Async](ticks: Stream[F, Unit], ping: F[Unit], restartDelay: FiniteDuration)(using
      log: LogWriter[F]
  ): Stream[F, Unit] =
    ticks
      .evalMap(_ => log.info("[HealthcheckPing] heartbeat") >> ping)
      .handleErrorWith(e =>
        Stream.exec(log.error(s"[HealthcheckPing] The heartbeat failed, starting it again: $e")) ++
          Stream.sleep_(restartDelay) ++
          resilient(ticks = ticks, ping = ping, restartDelay = restartDelay)
      )

  def sendHealthcheckEndpoint[F[_]: Async](dbLog: DBLog[F], client: Client[F], healthcheckEndpoint: Uri): F[Boolean] =
    dbLog
      .getLastLog()
      .flatMap(latestLogMessage =>
        client.successful(
          Request(
            method = Method.POST,
            uri = healthcheckEndpoint
          ).withEntity(s"Latest log: $latestLogMessage")
        )
      )

  /** The application is booting. */
  def start[F[_]: Async](client: Client[F], healthcheckEndpoint: Uri)(using log: LogWriter[F]): F[Unit] =
    signal(client = client, healthcheckEndpoint = healthcheckEndpoint, path = "start", body = "Starting")

  /** The application is going down because of an error: the check fails now, not when the next heartbeat is missed. */
  def fail[F[_]: Async](client: Client[F], healthcheckEndpoint: Uri, reason: String)(using log: LogWriter[F]): F[Unit] =
    signal(client = client, healthcheckEndpoint = healthcheckEndpoint, path = "fail", body = reason)

  private def signal[F[_]: Async](client: Client[F], healthcheckEndpoint: Uri, path: String, body: String)(using
      log: LogWriter[F]
  ): F[Unit] =
    client
      .successful(Request[F](method = Method.POST, uri = healthcheckEndpoint / path).withEntity(body))
      .timeout(10.seconds)
      .flatMap(ok => log.warn(s"[HealthcheckPing] The /$path ping was not accepted").unlessA(ok))
      .handleErrorWith(e => log.warn(s"[HealthcheckPing] The /$path ping could not be sent: $e"))
}
