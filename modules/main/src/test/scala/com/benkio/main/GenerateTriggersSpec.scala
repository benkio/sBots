package com.benkio.main

import cats.effect.ExitCode
import com.benkio.telegrambotinfrastructure.SBot
import com.benkio.ABarberoBot.ABarberoBot
import com.benkio.CalandroBot.CalandroBot
import com.benkio.M0sconiBot.M0sconiBot
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import com.benkio.YouTuboAncheI0Bot.YouTuboAncheI0Bot
import munit.CatsEffectSuite

import java.io.*
import scala.concurrent.duration.Duration

class GenerateTriggersSpec extends CatsEffectSuite {

  override val munitIOTimeout = Duration(1, "m")

  test("GenerateTriggers.run should modify the expected files") {
    val calaSBotConfig           = SBot.buildSBotConfig(CalandroBot.sBotInfo)
    val abarSBotConfig           = SBot.buildSBotConfig(ABarberoBot.sBotInfo)
    val mosSBotConfig            = SBot.buildSBotConfig(M0sconiBot.sBotInfo)
    val ytaiSBotConfig           = SBot.buildSBotConfig(YouTuboAncheI0Bot.sBotInfo)
    val triggerFiles: List[File] = List(
      File(s"../bots/ABarberoBot/${abarSBotConfig.triggerFilename}"),
      File(s"../bots/M0sconiBot/${mosSBotConfig.triggerFilename}"),
      File(s"../bots/CalandroBot/${calaSBotConfig.triggerFilename}"),
      File(s"../bots/RichardPHJBensonBot/${RichardPHJBensonBot.sBotConfig.triggerFilename}"),
      File(s"../bots/YouTuboAncheI0Bot/${ytaiSBotConfig.triggerFilename}")
    )
    val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
    GenerateTriggers
      .run(List.empty)
      .map(exitCode => {

        assert(exitCode == ExitCode.Success)
        triggerFiles.foreach { file =>
          assert(file.exists(), s"${file.getAbsolutePath} should exist")
          assert(
            file.lastModified() >= fiveMinutesAgo,
            s"${file.getAbsolutePath} should have been modified in the last 5 minutes (lastModified=${file.lastModified()}, fiveMinutesAgo=$fiveMinutesAgo)"
          )
        }
      })
  }
}
