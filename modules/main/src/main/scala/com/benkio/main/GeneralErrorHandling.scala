package com.benkio.main

import cats.effect.kernel.Resource.ExitCase
import cats.effect.syntax.monadCancel.*
import cats.effect.Async
import cats.effect.ExitCode
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chatcore.paths.SBotPaths
import log.effect.LogWriter

import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

object GeneralErrorHandling {

  /** The crash history file is opt-in: without `sbots.logFile` nothing is written to the working directory. */
  def logFile: Option[Path] = SBotPaths.logFile

  /** Logs how the server ended (and, when configured, the history file). `onError` runs when it crashed (not on a
    * shutdown), e.g. to signal the failure.
    */
  def logAndDie[F[_]: Async, A](server: Resource[F, A], onError: String => F[Unit])(using
      log: LogWriter[F]
  ): Resource[F, A] =
    logAndDie(server = server, onError = onError, history = logFile)

  def logAndDie[F[_]: Async](app: F[ExitCode], onError: String => F[Unit])(using
      log: LogWriter[F]
  ): F[ExitCode] =
    logAndDie(app = app, onError = onError, history = logFile)

  private[main] def logAndDie[F[_]: Async, A](
      server: Resource[F, A],
      onError: String => F[Unit],
      history: Path
  )(using log: LogWriter[F]): Resource[F, A] =
    logAndDie(server = server, onError = onError, history = Some(history))

  private[main] def logAndDie[F[_]: Async](
      app: F[ExitCode],
      onError: String => F[Unit],
      history: Path
  )(using log: LogWriter[F]): F[ExitCode] =
    logAndDie(app = app, onError = onError, history = Some(history))

  private def logAndDie[F[_]: Async, A](
      server: Resource[F, A],
      onError: String => F[Unit],
      history: Option[Path]
  )(using log: LogWriter[F]): Resource[F, A] =
    server.onFinalizeCase {
      case ExitCase.Succeeded =>
        logMessage(history = history, error = s"Terminated with no error: ${ExitCase.Succeeded}")
      case ExitCase.Errored(e) =>
        crashed(history = history, e = e, onError = onError)
      case ExitCase.Canceled =>
        logMessage(history = history, error = "Cancelled}")
    }

  private def logAndDie[F[_]: Async](
      app: F[ExitCode],
      onError: String => F[Unit],
      history: Option[Path]
  )(using log: LogWriter[F]): F[ExitCode] =
    app
      .onCancel(logMessage(history = history, error = "Cancelled"))
      .flatTap(_ => logMessage(history = history, error = s"Terminated with no error: ${ExitCase.Succeeded}"))
      .handleErrorWith((e: Throwable) => crashed(history = history, e = e, onError = onError).as(ExitCode.Error))

  private def crashed[F[_]: Async](history: Option[Path], e: Throwable, onError: String => F[Unit])(using
      log: LogWriter[F]
  ): F[Unit] = {
    val error = s"Terminated with Error 🚫: ${e.getMessage}"
    logMessage(history = history, error = error, stackTrace = Some(stackTrace(e))) >> onError(error)
  }

  private def stackTrace(e: Throwable): String = {
    val writer = new StringWriter()
    e.printStackTrace(new PrintWriter(writer))
    writer.toString
  }

  /** Always logs the outcome. The history is a file that only grows: when configured, every end is appended with its
    * time (and the stack trace of a crash), so previous ones are not lost when the application restarts.
    */
  private def logMessage[F[_]: Async](
      history: Option[Path],
      error: String,
      stackTrace: Option[String] = None
  )(using
      log: LogWriter[F]
  ): F[Unit] =
    for {
      _ <- log.error(s"[Main] Exit Log: $error")
      _ <- history.traverse_ { path =>
        for {
          now <- Async[F].realTimeInstant
          entry = s"[$now] $error\n${stackTrace.getOrElse("")}"
          _ <- Async[F].blocking(
            Files.writeString(path, entry, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
          )
        } yield ()
      }
    } yield ()
}
