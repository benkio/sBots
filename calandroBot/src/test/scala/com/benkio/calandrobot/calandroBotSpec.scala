package com.benkio.calandrobot

import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botCapabilities.ResourceSource
import com.benkio.telegrambotinfrastructure.model.MediaFile
import munit.CatsEffectSuite

class CalandroBotSpec extends CatsEffectSuite {

  def testFilename(filename: String): IO[Unit] =
    ResourceSource
      .selectResourceAccess(CalandroBot.resourceSource)
      .getResourceByteArray[IO](filename)
      .use[Unit](fileBytes => assert(fileBytes.nonEmpty).pure[IO])

  test("commandRepliesData should never raise an exception when try to open the file in resounces") {
    CalandroBot.buildPollingBot[IO, Unit](bot =>
      bot
        .commandRepliesDataF[IO]
        .map(
          _.flatMap(_.mediafiles)
            .foreach((mf: MediaFile) => testFilename(mf.filename))
        )
    )
  }

  test("messageRepliesData should never raise an exception when try to open the file in resounces") {
    CalandroBot.messageRepliesData
      .flatMap(_.mediafiles)
      .foreach((mf: MediaFile) => testFilename(mf.filename))
  }
}
