package com.benkio.main

import cats.effect.ExitCode
import com.benkio.chatcore.paths.SBotPaths
import com.benkio.chattelegramadapter.SBot
import munit.CatsEffectSuite

import java.nio.file.Files
import java.nio.file.Path
import scala.concurrent.duration.Duration

class GenerateInstructionsSpec extends CatsEffectSuite {

  override val munitIOTimeout = Duration(1, "m")

  test("GenerateInstructions.run should generate up-to-date instructions files for every bot") {
    val instructionFiles: List[Path] = BotRegistry.toList(BotRegistry.value).map { entry =>
      val config = SBot.buildSBotConfig(entry.sBotInfo)
      SBotPaths.repoRoot
        .resolve("modules")
        .resolve("bots")
        .resolve(entry.sBotInfo.botName.value)
        .resolve(config.instructionsFilename)
    }
    val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
    for {
      exitCode  <- GenerateInstructions.run(List.empty)
      checkCode <- GenerateInstructions.run(List("--check"))
    } yield {
      assertEquals(exitCode, ExitCode.Success)
      assertEquals(checkCode, ExitCode.Success)
      instructionFiles.foreach { file =>
        assert(Files.exists(file), s"${file.toAbsolutePath().toString()} should exist")
        assert(
          Files.getLastModifiedTime(file).toMillis() >= fiveMinutesAgo,
          s"${file.toAbsolutePath().toString()} should have been modified in the last 5 minutes"
        )
      }
    }
  }
}
