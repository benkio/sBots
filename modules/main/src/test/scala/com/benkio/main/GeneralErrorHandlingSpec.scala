package com.benkio.main

import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.paths.SBotPaths
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite

import java.nio.file.Files
import java.nio.file.Path

// No timing involved on purpose: the failing resource/IO fail immediately and the log is written before
// `logAndDie` completes, so there is nothing to wait for (a sleep/race here made the test flaky on slow CI).
class GeneralErrorHandlingSpec extends CatsEffectSuite {

  val plainErrorMessage: String    = "Test Throwable"
  val expectedErrorMessage: String = "Terminated with Error 🚫: Test Throwable"
  val failedIO: IO[ExitCode]       =
    IO { throw new RuntimeException(plainErrorMessage) }
  val failedResource: Resource[IO, Unit] =
    Resource.eval(failedIO).void

  private def tempHistory: IO[java.nio.file.Path] =
    IO.blocking(java.nio.file.Files.createTempFile("log", ".txt"))
      .flatTap(h => IO.blocking(java.nio.file.Files.delete(h)))

  test("GeneralErrorHandling should write the history when an error occurred in the failed resource") {
    for {
      history <- tempHistory
      result  <- GeneralErrorHandling.logAndDie(failedResource, _ => IO.unit, history).use_.attempt
      content <- IO.blocking(java.nio.file.Files.readString(history))
    } yield {
      assertEquals(result.left.map(_.getMessage), Left(plainErrorMessage))
      assert(content.contains(expectedErrorMessage), content)
    }
  }

  test("GeneralErrorHandling should write the history when an error occurred in the failed IO") {
    for {
      history  <- tempHistory
      exitCode <- GeneralErrorHandling.logAndDie(failedIO, _ => IO.unit, history)
      content  <- IO.blocking(java.nio.file.Files.readString(history))
    } yield {
      assertEquals(exitCode, ExitCode.Error)
      assert(content.contains(expectedErrorMessage), content)
    }
  }

  test("GeneralErrorHandling should write the history when the IO ends normally or is cancelled") {
    for {
      completedHistory <- tempHistory
      exitCode         <- GeneralErrorHandling.logAndDie(IO.pure(ExitCode.Success), _ => IO.unit, completedHistory)
      completed        <- IO.blocking(java.nio.file.Files.readString(completedHistory))
      cancelledHistory <- tempHistory
      running          <- IO.deferred[Unit]
      fiber            <- GeneralErrorHandling
        .logAndDie(running.complete(()) >> IO.never[ExitCode], _ => IO.unit, cancelledHistory)
        .start
      _         <- running.get >> fiber.cancel
      cancelled <- IO.blocking(java.nio.file.Files.readString(cancelledHistory))
    } yield {
      assertEquals(exitCode, ExitCode.Success)
      assert(completed.contains("Terminated with no error"), completed)
      assert(cancelled.contains("Cancelled"), cancelled)
    }
  }

  test("GeneralErrorHandling should signal the error when the resource crashes, and nothing on a normal end") {
    for {
      signals <- Ref.of[IO, List[String]](List.empty)
      onError = (error: String) => signals.update(_ :+ error)
      _        <- GeneralErrorHandling.logAndDie(failedResource, onError).use_.attempt
      _        <- GeneralErrorHandling.logAndDie(Resource.unit[IO], onError).use_
      afterEnd <- signals.get
    } yield assertEquals(afterEnd, List(expectedErrorMessage))
  }

  test("GeneralErrorHandling should signal the error when the IO crashes") {
    for {
      signals <- Ref.of[IO, List[String]](List.empty)
      _       <- GeneralErrorHandling.logAndDie(failedIO, error => signals.update(_ :+ error))
      sent    <- signals.get
    } yield assertEquals(sent, List(expectedErrorMessage))
  }

  test("GeneralErrorHandling should not write a crash history when none is configured") {
    val implicitLog: Path        = SBotPaths.workingDirectory.resolve("log.txt")
    val propertyName: String     = SBotPaths.LogFileProperty
    val previous: Option[String] = Option(System.getProperty(propertyName))

    val unconfigured: Resource[IO, Unit] = Resource.make(
      IO.delay {
        System.clearProperty(propertyName)
        Files.deleteIfExists(implicitLog)
        ()
      }
    ) { _ =>
      IO.delay {
        previous match {
          case Some(value) =>
            System.setProperty(propertyName, value)
            ()
          case None =>
            System.clearProperty(propertyName)
            ()
        }
        Files.deleteIfExists(implicitLog)
        ()
      }
    }

    unconfigured.use { _ =>
      for {
        signals <- Ref.of[IO, List[String]](List.empty)
        _       <- GeneralErrorHandling.logAndDie(failedIO, error => signals.update(_ :+ error)).attempt
        sent    <- signals.get
      } yield {
        assertEquals(GeneralErrorHandling.logFile, None)
        assertEquals(sent, List(expectedErrorMessage))
        assert(Files.notExists(implicitLog), s"unexpected crash history at $implicitLog")
      }
    }
  }

  test("a crash is appended to the history with its stack trace, and the previous entries are kept") {
    for {
      history <- IO.blocking(java.nio.file.Files.createTempFile("log", ".txt"))
      _       <- IO.blocking(java.nio.file.Files.delete(history))
      _       <- GeneralErrorHandling.logAndDie(failedIO, _ => IO.unit, history)
      _       <- GeneralErrorHandling.logAndDie(failedIO, _ => IO.unit, history)
      content <- IO.blocking(java.nio.file.Files.readString(history))
    } yield {
      assertEquals(content.split("\\[[^\\]]+\\] ").count(_.nonEmpty), 2)
      assert(content.contains(expectedErrorMessage), content)
      assert(content.contains("java.lang.RuntimeException: Test Throwable"), content)
      assert(content.contains("\tat "), content)
    }
  }
}
