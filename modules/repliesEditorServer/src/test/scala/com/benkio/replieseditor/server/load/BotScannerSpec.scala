package com.benkio.replieseditor.server.load

import munit.CatsEffectSuite

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class BotScannerSpec extends CatsEffectSuite {

  private def mkBot(repoRoot: Path, botName: String, botId: String): Unit = {
    val botDir = repoRoot.resolve("modules").resolve("bots").resolve(botName)
    Files.createDirectories(botDir)
    Files.writeString(botDir.resolve(s"${botId}_list.json"), "[]", StandardCharsets.UTF_8)
    val resDir = botDir.resolve("src").resolve("main").resolve("resources")
    Files.createDirectories(resDir)
    Files.writeString(resDir.resolve(s"${botId}_replies.json"), "[]", StandardCharsets.UTF_8)
    ()
  }

  test("scanBots finds bots and builds BotFiles") {
    val repoRoot = Files.createTempDirectory("bot-scanner-test-").toAbsolutePath.normalize()
    Files.createDirectories(repoRoot.resolve("modules").resolve("bots"))
    mkBot(repoRoot = repoRoot, botName = "CalandroBot", botId = "cala")

    val scanner = BotScanner(repoRoot)
    scanner.scanBots().map { bots =>
      assertEquals(bots.map(_.botId), List("cala"))
      assertEquals(bots.head.botName, "CalandroBot")
      assert(bots.head.repliesJson.getFileName.toString.endsWith("cala_replies.json"))
      assert(bots.head.listJson.getFileName.toString.endsWith("cala_list.json"))
      assert(bots.head.triggersTxt.getFileName.toString.endsWith("cala_triggers.md"))
    }
  }
}
