package com.benkio.main

import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.main.Logger.given
import munit.CatsEffectSuite

// No timing involved on purpose: the failing resource/IO fail immediately and the log is written before
// `dbLogAndDie` completes, so there is nothing to wait for (a sleep/race here made the test flaky on slow CI).
class GeneralErrorHandlingSpec extends CatsEffectSuite {

  val plainErrorMessage: String    = "Test Throwable"
  val expectedErrorMessage: String = "Terminated with Error 🚫: Test Throwable"
  val failedIO: IO[ExitCode]       =
    IO { throw new RuntimeException(plainErrorMessage) }
  val failedResource: Resource[IO, Unit] =
    Resource.eval(failedIO).void

  test("GeneralErrorHandling should write logs when an error occurred in the failed resource") {
    val dbLayer = DBLayerMock.mock(SBotId("whateverBot"))
    for {
      result  <- GeneralErrorHandling.dbLogAndDie(dbLayer.dbLog, failedResource, _ => IO.unit).use_.attempt
      lastLog <- dbLayer.dbLog.getLastLog()
    } yield {
      assertEquals(result.left.map(_.getMessage), Left(plainErrorMessage))
      assertEquals(lastLog.map(_.message), Some(expectedErrorMessage))
    }
  }

  test("GeneralErrorHandling should write logs when an error occurred in the failed IO") {
    val dbLayer = DBLayerMock.mock(SBotId("whateverBot"))
    for {
      exitCode <- GeneralErrorHandling.dbLogAndDie(dbLayer.dbLog, failedIO, _ => IO.unit)
      lastLog  <- dbLayer.dbLog.getLastLog()
    } yield {
      assertEquals(exitCode, ExitCode.Error)
      assertEquals(lastLog.map(_.message), Some(expectedErrorMessage))
    }
  }

  test("GeneralErrorHandling should signal the error when the resource crashes, and nothing on a normal end") {
    val dbLayer = DBLayerMock.mock(SBotId("whateverBot"))
    for {
      signals <- Ref.of[IO, List[String]](List.empty)
      onError = (error: String) => signals.update(_ :+ error)
      _        <- GeneralErrorHandling.dbLogAndDie(dbLayer.dbLog, failedResource, onError).use_.attempt
      _        <- GeneralErrorHandling.dbLogAndDie(dbLayer.dbLog, Resource.unit[IO], onError).use_
      afterEnd <- signals.get
    } yield assertEquals(afterEnd, List(expectedErrorMessage))
  }

  test("GeneralErrorHandling should signal the error when the IO crashes") {
    val dbLayer = DBLayerMock.mock(SBotId("whateverBot"))
    for {
      signals <- Ref.of[IO, List[String]](List.empty)
      _       <- GeneralErrorHandling.dbLogAndDie(dbLayer.dbLog, failedIO, error => signals.update(_ :+ error))
      sent    <- signals.get
    } yield assertEquals(sent, List(expectedErrorMessage))
  }
}
