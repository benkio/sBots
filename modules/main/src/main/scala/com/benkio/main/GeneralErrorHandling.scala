package com.benkio.main

import cats.effect.kernel.Resource.ExitCase
import cats.effect.Async
import cats.effect.ExitCode
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chatcore.repository.db.DBLog
import log.effect.LogWriter

import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

object GeneralErrorHandling {

  val logFile: Path = Paths.get("log.txt")

  /** Logs how the server ended. `onError` runs when it crashed (not on a shutdown), e.g. to signal the failure. */
  def dbLogAndDie[F[_]: Async, A](dbLog: DBLog[F], server: Resource[F, A], onError: String => F[Unit])(using
      log: LogWriter[F]
  ): Resource[F, A] =
    dbLogAndDie(dbLog = dbLog, server = server, onError = onError, history = logFile)

  def dbLogAndDie[F[_]: Async](dbLog: DBLog[F], app: F[ExitCode], onError: String => F[Unit])(using
      log: LogWriter[F]
  ): F[ExitCode] =
    dbLogAndDie(dbLog = dbLog, app = app, onError = onError, history = logFile)

  private[main] def dbLogAndDie[F[_]: Async, A](
      dbLog: DBLog[F],
      server: Resource[F, A],
      onError: String => F[Unit],
      history: Path
  )(using log: LogWriter[F]): Resource[F, A] =
    server.onFinalizeCase {
      case ExitCase.Succeeded =>
        logMessage(dbLog = dbLog, history = history, error = s"Terminated with no error: ${ExitCase.Succeeded}")
      case ExitCase.Errored(e) =>
        crashed(dbLog = dbLog, history = history, e = e, onError = onError)
      case ExitCase.Canceled =>
        logMessage(dbLog = dbLog, history = history, error = "Cancelled}")
    }

  private[main] def dbLogAndDie[F[_]: Async](
      dbLog: DBLog[F],
      app: F[ExitCode],
      onError: String => F[Unit],
      history: Path
  )(using log: LogWriter[F]): F[ExitCode] =
    app.handleErrorWith((e: Throwable) =>
      crashed(dbLog = dbLog, history = history, e = e, onError = onError).as(ExitCode.Error)
    )

  private def crashed[F[_]: Async](dbLog: DBLog[F], history: Path, e: Throwable, onError: String => F[Unit])(using
      log: LogWriter[F]
  ): F[Unit] = {
    val error = s"Terminated with Error 🚫: ${e.getMessage}"
    logMessage(dbLog = dbLog, history = history, error = error, stackTrace = Some(stackTrace(e))) >> onError(error)
  }

  private def stackTrace(e: Throwable): String = {
    val writer = new StringWriter()
    e.printStackTrace(new PrintWriter(writer))
    writer.toString
  }

  /** The history is a file that only grows: every end is appended with its time (and the stack trace of a crash), so
    * the previous ones are not lost when the application restarts.
    */
  private def logMessage[F[_]: Async](
      dbLog: DBLog[F],
      history: Path,
      error: String,
      stackTrace: Option[String] = None
  )(using
      log: LogWriter[F]
  ): F[Unit] =
    for {
      _   <- log.error(s"[Main] Exit Log: $error")
      _   <- dbLog.writeLog(error)
      now <- Async[F].realTimeInstant
      entry = s"[$now] $error\n${stackTrace.getOrElse("")}"
      _ <- Async[F].blocking(
        Files.writeString(history, entry, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
      )
    } yield ()
}
