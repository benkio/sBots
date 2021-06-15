package com.benkio.calandrobot

import com.benkio.telegrambotinfrastructure.botCapabilities.ResourceSource
import org.scalatest._
import cats.effect._
import com.benkio.telegrambotinfrastructure.model.MediaFile
import org.scalatest.wordspec.AnyWordSpec

class CalandroBotSpec extends AnyWordSpec {

  implicit val timerIO: Timer[IO]               = IO.timer(scala.concurrent.ExecutionContext.global)
  implicit val contextshiftIO: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)

  def testFilename(filename: String): Assertion =
    if (
      Effect
        .toIOFromRunAsync(
          ResourceSource
            .selectResourceAccess(CalandroBot.resourceSource)
            .getResourceByteArray[IO](filename)
            .use[IO, Array[Byte]](x => IO.pure(x))
        )
        .unsafeRunSync()
        .isEmpty
    )
      fail(s"$filename cannot be found")
    else succeed

  "commandRepliesData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        CalandroBot.buildBot[IO, Unit](
          scala.concurrent.ExecutionContext.global,
          bot =>
            IO(
              bot.commandRepliesData
                .flatMap(_.mediafiles)
                .foreach((mf: MediaFile) => testFilename(mf.filename))
            )
        )
      }
    }
  }

  "messageRepliesData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        CalandroBot.messageRepliesData
          .flatMap(_.mediafiles)
          .foreach((mf: MediaFile) => testFilename(mf.filename))
      }
    }
  }
}
