package com.benkio.main

import cats.effect.implicits.*
import cats.effect.Async
import cats.implicits.*
import com.benkio.chatcore.repository.db.DBLog
import cron4s.CronExpr
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.Stream
import org.http4s.*
import org.http4s.client.Client

object HealthcheckPing {

  def healthcheckPing[F[_]: Async](
      dbLog: DBLog[F],
      client: Client[F],
      healthcheckEndpoint: Uri,
      healthcheckCron: CronExpr
  ): F[Unit] = {
    val cronScheduler     = Cron4sScheduler.systemDefault[F]
    val healthcheckStream =
      cronScheduler.awakeEvery(healthcheckCron) >>
        Stream.eval(
          sendHealthcheckEndpoint(dbLog = dbLog, client = client, healthcheckEndpoint = healthcheckEndpoint).void
        )
    healthcheckStream.compile.drain.start.void
  }

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
}
