package com.benkio.main

import cats.effect.ExitCode
import com.benkio.chattelegramadapter.SBot
import munit.CatsEffectSuite

import java.nio.file.Files
import java.nio.file.Path
import scala.concurrent.duration.Duration

class GenerateTriggersSpec extends CatsEffectSuite {

  override val munitIOTimeout = Duration(1, "m")

  test("GenerateTriggers.run should modify the expected files") {
    val triggerFiles: List[Path] = BotRegistry.toList(BotRegistry.value).map { entry =>
      val config = SBot.buildSBotConfig(entry.sBotInfo)
      Path.of(s"../bots/${entry.sBotInfo.botName.value}/${config.triggerFilename}")
    }
    val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
    GenerateTriggers
      .run(List.empty)
      .map(exitCode => {

        assert(exitCode == ExitCode.Success)
        triggerFiles.foreach { file =>
          assert(Files.exists(file), s"${file.toAbsolutePath().toString()} should exist")
          assert(
            Files.getLastModifiedTime(file).toMillis() >= fiveMinutesAgo,
            s"${file.toAbsolutePath().toString()} should have been modified in the last 5 minutes (lastModified=${Files.getLastModifiedTime(file).toMillis()}, fiveMinutesAgo=$fiveMinutesAgo)"
          )
        }
      })
  }
}
