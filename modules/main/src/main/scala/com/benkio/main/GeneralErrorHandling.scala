package com.benkio.main

import cats.effect.kernel.Resource.ExitCase
import cats.effect.Async
import cats.effect.ExitCode
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chatcore.repository.db.DBLog
import log.effect.LogWriter

object GeneralErrorHandling {

  def dbLogAndDie[F[_]: Async, A](dbLog: DBLog[F], server: Resource[F, A])(using
      log: LogWriter[F]
  ): Resource[F, A] =
    server.onFinalizeCase {
      case ExitCase.Succeeded =>
        logMessage(dbLog = dbLog, error = s"Terminated with no error: ${ExitCase.Succeeded}")
      case ExitCase.Errored(e) =>
        logMessage(dbLog = dbLog, error = s"Terminated with Error 🚫: ${e.getMessage}")
      case ExitCase.Canceled =>
        logMessage(dbLog = dbLog, error = s"Cancelled}")
    }

  def dbLogAndDie[F[_]: Async](dbLog: DBLog[F], app: F[ExitCode])(using
      log: LogWriter[F]
  ): F[ExitCode] =
    app.handleErrorWith((e: Throwable) =>
      logMessage(dbLog, s"Terminated with Error 🚫: ${e.getMessage}").as(ExitCode.Error)
    )

  private def logMessage[F[_]: Async](dbLog: DBLog[F], error: String)(using
      log: LogWriter[F]
  ): F[Unit] =
    for {
      _ <- log.error(s"[Main] Exit Log: $error")
      _ <- dbLogError[F](dbLog, error)
    } yield ()

  private def dbLogError[F[_]](dbLog: DBLog[F], error: String): F[Unit] =
    dbLog.writeLog(error)
}
