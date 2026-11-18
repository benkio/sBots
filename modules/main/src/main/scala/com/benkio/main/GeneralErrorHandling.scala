package com.benkio.main

import cats.effect.kernel.Resource.ExitCase
import cats.effect.Async
import cats.effect.ExitCode
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chatcore.repository.db.DBLog
import log.effect.LogWriter

import java.nio.file.Files
import java.nio.file.Paths

object GeneralErrorHandling {

  /** Logs how the server ended. `onError` runs when it crashed (not on a shutdown), e.g. to signal the failure. */
  def dbLogAndDie[F[_]: Async, A](dbLog: DBLog[F], server: Resource[F, A], onError: String => F[Unit])(using
      log: LogWriter[F]
  ): Resource[F, A] =
    server.onFinalizeCase {
      case ExitCase.Succeeded =>
        logMessage(dbLog = dbLog, error = s"Terminated with no error: ${ExitCase.Succeeded}")
      case ExitCase.Errored(e) =>
        val error = s"Terminated with Error 🚫: ${e.getMessage}"
        logMessage(dbLog = dbLog, error = error) >> onError(error)
      case ExitCase.Canceled =>
        logMessage(dbLog = dbLog, error = "Cancelled}")
    }

  def dbLogAndDie[F[_]: Async](dbLog: DBLog[F], app: F[ExitCode], onError: String => F[Unit])(using
      log: LogWriter[F]
  ): F[ExitCode] =
    app.handleErrorWith((e: Throwable) => {
      val error = s"Terminated with Error 🚫: ${e.getMessage}"
      logMessage(dbLog, error) >> onError(error).as(ExitCode.Error)
    })

  private def logMessage[F[_]: Async](dbLog: DBLog[F], error: String)(using
      log: LogWriter[F]
  ): F[Unit] =
    for {
      _ <- log.error(s"[Main] Exit Log: $error")
      _ <- dbLog.writeLog(error)
      _ <- Async[F].blocking(Files.writeString(Paths.get("log.txt"), error))
    } yield ()
}
