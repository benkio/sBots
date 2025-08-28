package com.benkio.main

import cats.effect.Async
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.repository.db.DBLog
import log.effect.LogWriter

object GeneralErrorHandling {

  def dbLogAndRestart[F[_]: Async, A](dbLog: DBLog[F], server: Resource[F, A])(using
      log: LogWriter[F]
  ): Resource[F, A] =
    server.handleErrorWith((e: Throwable) =>
      for {
        _       <- Resource.eval(log.error("[Main] ERROR: " + e.getMessage))
        _       <- Resource.eval(dbLogError(dbLog, e))
        restart <- dbLogAndRestart(dbLog, server)
      } yield restart
    )

  def dbLogAndRestart(dbLog: DBLog[IO], app: IO[ExitCode])(using
      log: LogWriter[IO]
  ): IO[ExitCode] =
    app.handleErrorWith((e: Throwable) =>
      for {
        _       <- log.error("[Main] ERROR: " + e.getMessage)
        _       <- dbLogError[IO](dbLog, e)
        restart <- dbLogAndRestart(dbLog, app)
      } yield restart
    )

  private def dbLogError[F[_]](dbLog: DBLog[F], e: Throwable): F[Unit] =
    dbLog.writeLog(e.getMessage())
}
