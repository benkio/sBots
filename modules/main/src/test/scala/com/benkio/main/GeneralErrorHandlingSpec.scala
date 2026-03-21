package com.benkio.main

import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.main.Logger.given
import munit.CatsEffectSuite

import java.io.OutputStream
import java.io.PrintStream
import scala.concurrent.duration.*

class GeneralErrorHandlingSpec extends CatsEffectSuite {

  val sleepTime                    = 100.millis
  val expectedErrorMessage: String = "Test Throwable"
  var failedIORun: Boolean         = true
  val failedIO: IO[ExitCode]       =
    IO {
      if failedIORun then {
        failedIORun = false
        throw new RuntimeException(expectedErrorMessage)
      } else ExitCode.Success
    }
  val failedResource: Resource[IO, Unit] =
    Resource
      .eval(
        failedIO
      )
      .void

  private def withMutedConsole[A](fa: IO[A]): IO[A] =
    IO {
      val originalOut = System.out
      val originalErr = System.err
      val sink        = new PrintStream(new OutputStream { override def write(b: Int): Unit = () })
      System.setOut(sink)
      System.setErr(sink)
      (originalOut, originalErr, sink)
    }.bracket { _ =>
      fa
    } { case (originalOut, originalErr, sink) =>
      IO { System.setOut(originalOut); System.setErr(originalErr); sink.close() }
    }

  test("GeneralErrorHandling should write logs when an error occurred in the failed resource") {
    val dbLayer = DBLayerMock.mock(SBotId("whateverBot"))
    failedIORun = true
    val computation: IO[Unit] =
      withMutedConsole(
        GeneralErrorHandling
          .dbLogAndRestart(dbLayer.dbLog, failedResource)
          .race(Resource.eval(IO.sleep(sleepTime)))
          .use_
      )

    val lastLogMessage: IO[Option[String]] = for {
      _       <- computation
      lastLog <- dbLayer.dbLog.getLastLog()
    } yield lastLog.map(_.message)
    assertIO(lastLogMessage, Some(expectedErrorMessage))
  }

  test("GeneralErrorHandling should write logs when an error occurred in the failed IO") {
    val dbLayer = DBLayerMock.mock(SBotId("whateverBot"))
    failedIORun = true
    val computation: IO[Unit] =
      withMutedConsole(
        GeneralErrorHandling
          .dbLogAndRestart(dbLayer.dbLog, failedIO)
          .race(IO.sleep(sleepTime))
          .void
      )

    val lastLogMessage: IO[Option[String]] = for {
      _       <- computation
      lastLog <- dbLayer.dbLog.getLastLog()
    } yield lastLog.map(_.message)
    assertIO(lastLogMessage, Some(expectedErrorMessage))
  }
}
