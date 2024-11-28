package com.benkio.main

import cats.effect.ExitCode
import com.benkio.abarberobot.ABarberoBot
import com.benkio.calandrobot.CalandroBot
import com.benkio.m0sconibot.M0sconiBot
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import com.benkio.youtuboanchei0bot.YouTuboAncheI0Bot
import java.io._
import java.time.Instant
import munit.CatsEffectSuite
import scala.concurrent.duration.*

class GenerateTriggersSpec extends CatsEffectSuite {

  test("GenerateTriggers.run should modify the expected files") {
    for
      exitCode <- GenerateTriggers.run(List.empty)
      triggerFiles: List[File] = List(
        File(s"../aBarberoBot/${ABarberoBot.triggerFilename}"),
        File(s"../m0sconiBot/${M0sconiBot.triggerFilename}"),
        File(s"../calandroBot/${CalandroBot.triggerFilename}"),
        File(s"../richardPHJBensonBot/${RichardPHJBensonBot.triggerFilename}"),
        File(s"../youTuboAncheI0Bot/${YouTuboAncheI0Bot.triggerFilename}"),
      )
    yield {
      assert(exitCode == ExitCode.Success)
      assert(triggerFiles.forall(f => Instant.now.toEpochMilli - f.lastModified < 2.seconds.toMillis))
    }
  }
}
