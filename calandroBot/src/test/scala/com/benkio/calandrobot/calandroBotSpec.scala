package com.benkio.calandrobot

import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccess
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccessSpec
import com.benkio.telegrambotinfrastructure.model.MediaFile
import log.effect.LogWriter
import log.effect.fs2.SyncLogWriter.consoleLog
import munit.CatsEffectSuite

class CalandroBotSpec extends CatsEffectSuite {

  implicit val log: LogWriter[IO] = consoleLog
  implicit val resourceAccess     = ResourceAccess.fromResources

  test("commandRepliesData should never raise an exception when try to open the file in resounces") {
    val result = CalandroBot.buildPollingBot[IO, Boolean](bot =>
      bot
        .commandRepliesDataF[IO]
        .flatMap(
          _.flatMap(_.mediafiles)
            .traverse((mf: MediaFile) => ResourceAccessSpec.testFilename(mf.filepath))
            .map(_.foldLeft(true)(_ && _))
        )
    )

    assertIO(result, true)
  }

  test("messageRepliesData should never raise an exception when try to open the file in resounces") {
    val result = CalandroBot
      .messageRepliesData[IO]
      .flatMap(_.mediafiles)
      .traverse((mf: MediaFile) => ResourceAccessSpec.testFilename(mf.filename))
      .map(_.foldLeft(true)(_ && _))

    assertIO(result, true)
  }
}
