package com.benkio.main

import cats.effect.*
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite

import scala.concurrent.duration.*

class GeneralErrorHandlingSpec extends CatsEffectSuite {

  given log: LogWriter[IO]         = consoleLogUpToLevel(LogLevels.Error)
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
        .race(Resource.eval(IO.sleep(20.millis)))
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
        .race(IO.sleep(20.millis))
        .void

    val lastLogMessage: IO[Option[String]] = for {
      _       <- computation
      lastLog <- emptyDBLayer.dbLog.getLastLog()
    } yield lastLog.map(_.message)
    assertIO(lastLogMessage, Some(expectedErrorMessage))
  }
}
