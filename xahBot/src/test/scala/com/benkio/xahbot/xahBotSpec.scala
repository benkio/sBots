package com.benkio.xahbot

import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botCapabilities.ResourceSource
import com.benkio.telegrambotinfrastructure.model.MediaFile
import munit.CatsEffectSuite

class XahBotSpec extends CatsEffectSuite {

  def testFilename(filename: String): IO[Unit] =
    ResourceSource
      .selectResourceAccess(XahBot.resourceSource)
      .getResourceByteArray[IO](filename)
      .use[Unit](fileBytes => assert(fileBytes.nonEmpty).pure[IO])

  test("commandRepliesData should never raise an exception when try to open the file in resounces") {
    XahBot.buildBot[IO, Unit](
      scala.concurrent.ExecutionContext.global,
      bot =>
        for {
          commandRepliesData <- bot.commandRepliesDataF
        } yield commandRepliesData
          .flatMap(_.mediafiles)
          .foreach((mf: MediaFile) => testFilename(mf.filename))
    )
  }
}
