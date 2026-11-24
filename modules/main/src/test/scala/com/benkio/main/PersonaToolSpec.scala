package com.benkio.main

import cats.effect.ExitCode
import com.benkio.chatcore.paths.SBotPaths
import munit.CatsEffectSuite

import java.nio.file.Files
import java.nio.file.Path
import scala.concurrent.duration.Duration

class PersonaToolSpec extends CatsEffectSuite {

  override val munitIOTimeout = Duration(1, "m")

  test("PersonaTool --check succeeds and all persona/catalogue files exist") {
    val files: List[Path] = BotRegistry.toList(BotRegistry.value).flatMap { entry =>
      val id           = entry.sBotInfo.botId.value
      val resourcesDir = SBotPaths.repoRoot
        .resolve("modules")
        .resolve("bots")
        .resolve(entry.sBotInfo.botName.value)
        .resolve("src")
        .resolve("main")
        .resolve("resources")
      List(
        resourcesDir.resolve(s"${id}_persona.md"),
        resourcesDir.resolve(s"${id}_reply_catalogue.json")
      )
    }

    PersonaTool.run(List("--check")).map { exitCode =>
      assertEquals(exitCode, ExitCode.Success)
      files.foreach { file =>
        assert(Files.exists(file), s"${file.toAbsolutePath} should exist")
      }
    }
  }
}
