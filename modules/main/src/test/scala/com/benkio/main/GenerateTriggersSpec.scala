package com.benkio.main

import cats.effect.ExitCode
import com.benkio.abarberobot.ABarberoBot
import com.benkio.calandrobot.CalandroBot
import com.benkio.m0sconibot.M0sconiBot
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import com.benkio.youtuboanchei0bot.YouTuboAncheI0Bot
import munit.CatsEffectSuite

import java.io.*
import scala.concurrent.duration.Duration

class GenerateTriggersSpec extends CatsEffectSuite {

  override val munitIOTimeout = Duration(1, "m")

  test("GenerateTriggers.run should modify the expected files") {
    for {
      exitCode <- GenerateTriggers.run(List.empty)
      triggerFiles: List[File] = List(
        File(s"../bots/aBarberoBot/${ABarberoBot.sBotConfig.triggerFilename}"),
        File(s"../bots/m0sconiBot/${M0sconiBot.sBotConfig.triggerFilename}"),
        File(s"../bots/calandroBot/${CalandroBot.sBotConfig.triggerFilename}"),
        File(s"../bots/calandroBot/${CalandroBot.sBotConfig.repliesJsonFilename}"),
        File(s"../bots/richardPHJBensonBot/${RichardPHJBensonBot.sBotConfig.triggerFilename}"),
        File(s"../bots/youTuboAncheI0Bot/${YouTuboAncheI0Bot.sBotConfig.triggerFilename}")
      )
    } yield {
      assert(exitCode == ExitCode.Success)
    }
  }
}
