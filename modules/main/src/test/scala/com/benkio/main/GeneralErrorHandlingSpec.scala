package com.benkio.main

import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.main.Logger.given
import munit.CatsEffectSuite

import scala.concurrent.duration.*

class GeneralErrorHandlingSpec extends CatsEffectSuite {

  val sleepTime                    = 100.millis
  val plainErrorMessage: String    = "Test Throwable"
  val expectedErrorMessage: String = "Terminated with Error 🚫: Test Throwable"
  val failedIO: IO[ExitCode]       =
    IO { throw new RuntimeException(plainErrorMessage) }
  val failedResource: Resource[IO, Unit] =
    Resource.eval(failedIO).void

  test("GeneralErrorHandling should write logs when an error occurred in the failed resource") {
    val dbLayer               = DBLayerMock.mock(SBotId("whateverBot"))
    val computation: IO[Unit] =
      GeneralErrorHandling
        .dbLogAndDie(dbLayer.dbLog, failedResource)
        .race(Resource.eval(IO.sleep(sleepTime)))
        .use_

    val lastLogMessage: IO[Option[String]] = for {
      result  <- computation.attempt
      lastLog <- dbLayer.dbLog.getLastLog()
    } yield result.fold(_ => lastLog.map(_.message), _ => None)
    assertIO(lastLogMessage, Some(expectedErrorMessage))
  }

  test("GeneralErrorHandling should write logs when an error occurred in the failed IO") {
    val dbLayer               = DBLayerMock.mock(SBotId("whateverBot"))
    val computation: IO[Unit] =
      GeneralErrorHandling
        .dbLogAndDie(dbLayer.dbLog, failedIO)
        .race(IO.sleep(sleepTime))
        .void

    val lastLogMessage: IO[Option[String]] = for {
      _       <- computation
      lastLog <- dbLayer.dbLog.getLastLog()
    } yield lastLog.map(_.message)
    assertIO(lastLogMessage, Some(expectedErrorMessage))
  }
}
