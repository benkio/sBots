package com.benkio.calandrobot

import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botCapabilities.ResourceAccessSpec
import com.benkio.telegrambotinfrastructure.model.MediaFile
import munit.CatsEffectSuite

class CalandroBotSpec extends CatsEffectSuite {

  test("commandRepliesData should never raise an exception when try to open the file in resounces") {
    val result = CalandroBot.buildPollingBot[IO, Boolean](bot =>
      bot
        .commandRepliesDataF[IO]
        .flatMap(
          _.flatMap(_.mediafiles)
            .traverse((mf: MediaFile) => ResourceAccessSpec.testFilename(mf.filepath, CalandroBot.resourceSource))
            .map(_.foldLeft(true)(_ && _))
        )
    )

    assertIO(result, true)
  }

  test("messageRepliesData should never raise an exception when try to open the file in resounces") {
    val result = CalandroBot
      .messageRepliesData[IO]
      .flatMap(_.mediafiles)
      .traverse((mf: MediaFile) => ResourceAccessSpec.testFilename(mf.filename, CalandroBot.resourceSource))
      .map(_.foldLeft(true)(_ && _))

    assertIO(result, true)
  }
}
