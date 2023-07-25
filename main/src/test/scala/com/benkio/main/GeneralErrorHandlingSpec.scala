package com.benkio.main

import cats.effect.ExitCode
import cats.effect._
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import munit.CatsEffectSuite

import scala.concurrent.duration._

class GeneralErrorHandlingSpec extends CatsEffectSuite {

  val expectedErrorMessage: String = "Test Throwable"
  val failedResource: Resource[IO, Unit] =
    Resource.raiseError(new Throwable(expectedErrorMessage))
  val failedIO: IO[ExitCode] =
    IO.raiseError(new Throwable(expectedErrorMessage)).as(ExitCode.Error)

  test("GeneralErrorHandling should write logs when an error occurred in the failed resource") {
    val emptyDBLayer = DBLayerMock.mock("whateverBot")
    val computation: IO[Unit] =
      GeneralErrorHandling
        .dbLogAndRestart(emptyDBLayer.dbLog, failedResource)
        .race(Resource.eval(IO.sleep(2.seconds)))
        .use_

    val lastLogMessage: IO[Option[String]] = for {
      _       <- computation
      lastLog <- emptyDBLayer.dbLog.getLastLog()
    } yield lastLog.map(_.message)
    assertIO(lastLogMessage, Some(expectedErrorMessage))
  }

  test("GeneralErrorHandling should write logs when an error occurred in the failed IO") {
    val emptyDBLayer = DBLayerMock.mock("whateverBot")
    val computation: IO[Unit] =
      GeneralErrorHandling
        .dbLogAndRestart(emptyDBLayer.dbLog, failedIO)
        .race(IO.sleep(2.seconds))
        .void

    val lastLogMessage: IO[Option[String]] = for {
      _       <- computation
      lastLog <- emptyDBLayer.dbLog.getLastLog()
    } yield lastLog.map(_.message)
    assertIO(lastLogMessage, Some(expectedErrorMessage))
  }
}
