package com.benkio.xahbot

import cats.effect._
import com.benkio.telegrambotinfrastructure.botCapabilities.ResourceSource
import com.benkio.telegrambotinfrastructure.model.MediaFile
import org.scalatest._
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.Temporal

class XahBotSpec extends AnyWordSpec {

  implicit val timerIO: Temporal[IO]               = IO.timer(scala.concurrent.ExecutionContext.global)
  implicit val contextshiftIO: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)

  def testFilename(filename: String): Assertion =
    Effect
      .toIOFromRunAsync(
        ResourceSource
          .selectResourceAccess(XahBot.resourceSource)
          .getResourceByteArray[IO](filename)
          .use[IO, Array[Byte]](IO.pure)
          .attempt
      )
      .unsafeRunSync()
      .filterOrElse(_.nonEmpty, (x: Array[Byte]) => x)
      .fold(_ => fail(s"$filename cannot be found"), _ => succeed)

  "commandRepliesData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        XahBot.buildBot[IO, Unit](
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
}
