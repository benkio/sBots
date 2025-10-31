package com.benkio.main

import cats.effect.ExitCode
import com.benkio.abarberobot.ABarberoBot
import com.benkio.calandrobot.CalandroBot
import com.benkio.m0sconibot.M0sconiBot
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import com.benkio.youtuboanchei0bot.YouTuboAncheI0Bot
import munit.CatsEffectSuite

import java.io.*
import java.time.Instant

class GenerateTriggersSpec extends CatsEffectSuite {

  test("GenerateTriggers.run should modify the expected files") {
    for
      exitCode <- GenerateTriggers.run(List.empty)
      triggerFiles: List[File] = List(
        File(s"../bots/aBarberoBot/${ABarberoBot.triggerFilename}"),
        File(s"../bots/m0sconiBot/${M0sconiBot.triggerFilename}"),
        File(s"../bots/calandroBot/${CalandroBot.triggerFilename}"),
        File(s"../bots/richardPHJBensonBot/${RichardPHJBensonBot.triggerFilename}"),
        File(s"../bots/youTuboAncheI0Bot/${YouTuboAncheI0Bot.triggerFilename}")
      )
    yield {
      assert(exitCode == ExitCode.Success)
      assert(
        triggerFiles.forall(f => Instant.now.toEpochMilli - f.lastModified < munitIOTimeout.toMillis)
      ) // 30 seconds
    }
  }
}
