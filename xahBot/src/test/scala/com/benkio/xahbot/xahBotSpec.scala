package com.benkio.xahbot

import cats.effect.IO
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccessSpec
import com.benkio.telegrambotinfrastructure.model.MediaFile
import log.effect.LogWriter
import log.effect.fs2.SyncLogWriter.consoleLog
import munit.CatsEffectSuite

class XahBotSpec extends CatsEffectSuite {

  implicit val log: LogWriter[IO] = consoleLog

  test("commandRepliesData should never raise an exception when try to open the file in resounces") {
    val result = XahBot.buildPollingBot[IO, Boolean](bot =>
      for {
        commandRepliesData <- bot.commandRepliesDataF[IO]
        result <- commandRepliesData
          .flatMap(_.mediafiles)
          .traverse((mf: MediaFile) => ResourceAccessSpec.testFilename(mf.filepath, XahBot.resourceSource))
          .map(_.foldLeft(true)(_ && _))
      } yield result
    )
    assertIO(result, true)
  }
}
