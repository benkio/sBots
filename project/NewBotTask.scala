import sbt.*
import sbt.io.IO
import sbt.io.Path

import java.io.File

object NewBotTask {

  /** Copies the bot template from modules/bots/_template to modules/bots/botName and replaces TemplateBot -> botName,
    * tpl -> id in paths and file contents.
    */
  def createFromTemplate(base: File, botName: String, id: String): Unit = {
    val templateDir = base / "modules" / "bots" / "_template"
    val targetDir   = base / "modules" / "bots" / botName

    if (!templateDir.exists())
      throw new MessageOnlyException(s"Template not found: $templateDir")
    if (targetDir.exists())
      throw new MessageOnlyException(s"Target already exists: $targetDir")

    copyAndSubstitute(templateDir, targetDir, templateDir, botName, id)
    addBotToBotDBApplicationConf(base, botName, id)
    addBotToV1CreateBotTable(base, botName, id)
    addBotToDeployWorkflow(base, botName, id)
    addBotToMediaIntegritySpec(base, botName)
    addBotToFilesCheckConfig(base, botName, id)
    println(s"Created bot module at $targetDir")
    println(
      s"Next: git add modules/bots/$botName/src/main/resources/${id}_replies.json and modules/bots/$botName/src/main/resources/${id}_commands.json; then define the project in build.sbt, add to BotsRegistry (or run: ./scripts/CompleteBotRegistration.sc $botName $id). See docs/adding-a-bot.md"
    )
  }

  private def addBotToBotDBApplicationConf(base: File, botName: String, id: String): Unit = {
    val appConf = base / "modules" / "botDB" / "src" / "main" / "resources" / "application.conf"
    if (!appConf.isFile) return
    var content = IO.read(appConf)
    if (content.contains(s"""bot-id = "$id"""")) return // already added
    content = content.replace(
      """        { bot-id = "ytai", value = "../../../../bots/YouTuboAncheI0Bot"},""",
      s"""        { bot-id = "ytai", value = "../../../../bots/YouTuboAncheI0Bot"},
         |        { bot-id = "$id", value = "../../../../bots/$botName"},""".stripMargin
    )
    content = content.replace(
      """            {
        |                bot-id        = "ytai",
        |                caption-language = "it",
        |                youtube-sources = ["@youtuboancheio1365"],
        |                output-file-path = "../bots/YouTuboAncheI0Bot/ytai_shows.json"
        |            },""".stripMargin,
      s"""            {
         |                bot-id        = "ytai",
         |                caption-language = "it",
         |                youtube-sources = ["@youtuboancheio1365"],
         |                output-file-path = "../bots/YouTuboAncheI0Bot/ytai_shows.json"
         |            },
         |            {
         |                bot-id        = "$id",
         |                caption-language = "it",
         |                youtube-sources = [],
         |                output-file-path = "../bots/$botName/${id}_shows.json"
         |            },""".stripMargin
    )
    IO.write(appConf, content)
    println(s"Updated botDB application.conf with $botName ($id)")
  }

  private def addBotToV1CreateBotTable(base: File, botName: String, id: String): Unit = {
    val sqlFile =
      base / "modules" / "botDB" / "src" / "main" / "resources" / "db" / "migrations" / "V1__CreateBotTable.sql"
    if (!sqlFile.isFile) return
    var content = IO.read(sqlFile)
    if (content.contains(s"VALUES ('$id'")) return // already added
    // Insert before the test bot line so new bot is with the others
    val insertLine = s"INSERT INTO bot (id, bot_name, bot_full_name) VALUES ('$id', '$botName', '$botName');"
    content = content.replace(
      "INSERT INTO bot (id, bot_name, bot_full_name) VALUES ('ytai', 'YouTuboAncheI0Bot', 'Omar Palermo');",
      s"INSERT INTO bot (id, bot_name, bot_full_name) VALUES ('ytai', 'YouTuboAncheI0Bot', 'Omar Palermo');\n$insertLine"
    )
    IO.write(sqlFile, content)
    println(s"Updated db/migrations/V1__CreateBotTable.sql with $botName ($id)")
  }

  private def addBotToDeployWorkflow(base: File, botName: String, id: String): Unit = {
    val deployYml = base / ".github" / "workflows" / "deploy.yml"
    if (!deployYml.isFile) return
    val secretName = s"${id.toUpperCase}_TOKEN"
    if (secretName == "TOKEN") return
    var content = IO.read(deployYml)
    if (content.contains(secretName)) return // already added
    val tokenLine =
      s"""          printf '$${{ secrets.$secretName }}' > /home/runner/work/sBots/sBots/modules/bots/$botName/src/main/resources/${id}_$botName.token"""
    content = content.replace(
      """          printf '$${{ secrets.YTAI_TOKEN }}' > /home/runner/work/sBots/sBots/modules/bots/YouTuboAncheI0Bot/src/main/resources/ytai_YouTuboAncheI0Bot.token"""",
      s"""          printf '$${{ secrets.YTAI_TOKEN }}' > /home/runner/work/sBots/sBots/modules/bots/YouTuboAncheI0Bot/src/main/resources/ytai_YouTuboAncheI0Bot.token
          $tokenLine""""
    )
    IO.write(deployYml, content)
    println(s"Updated .github/workflows/deploy.yml with $secretName (add the secret in the repo)")
  }

  private def addBotToMediaIntegritySpec(base: File, botName: String): Unit = {
    val mediaIntegritySpec =
      base / "modules" / "integration-tests" / "src" / "test" / "scala" / "com" / "benkio" / "integration" / "integrationscalatest" / "main" / "MediaIntegritySpec.scala"
    if (!mediaIntegritySpec.isFile) return
    var content = IO.read(mediaIntegritySpec)

    val importLine = s"import com.benkio.$botName.$botName"
    if (!content.contains(importLine)) {
      content = content.replace(
        "import com.benkio.YouTuboAncheI0Bot.YouTuboAncheI0Bot\nimport org.scalatest.*",
        s"import com.benkio.YouTuboAncheI0Bot.YouTuboAncheI0Bot\n$importLine\nimport org.scalatest.*"
      )
    }

    val mediaVarName = s"${botName.take(1).toLowerCase}${botName.drop(1)}Files"
    if (!content.contains(s"SBot.buildSBotConfig($botName.sBotInfo)")) {
      val xahAnchor  = "      xahLeeFiles <- Resource.eval("
      val mediaBlock =
        s"""      $mediaVarName <- Resource.eval(
           |        mediaFilesFromBot(
           |          SBot.buildSBotConfig($botName.sBotInfo),
           |          (setup, msgData, cmdData) => {
           |            given telegramium.bots.high.Api[IO] = setup.api
           |            new SBotPolling[IO](setup, msgData, cmdData)
           |          }
           |        )
           |      )
           |""".stripMargin
      content = content.replace(xahAnchor, mediaBlock + xahAnchor)
    }

    if (!content.contains(s"++ $mediaVarName ++")) {
      content = content.replace(
        "++ xahLeeFiles)",
        s"++ $mediaVarName ++ xahLeeFiles)"
      )
    }

    IO.write(mediaIntegritySpec, content)
    println(s"Updated MediaIntegritySpec with $botName")
  }

  private def addBotToFilesCheckConfig(base: File, botName: String, id: String): Unit = {
    val filesCheckConfig = base / "filesCheck" / "src" / "Config.ts"
    if (!filesCheckConfig.isFile) return
    var content = IO.read(filesCheckConfig)

    val botJsonPath = s"'../modules/bots/$botName/${id}_list.json'"
    if (content.contains(s"id: '$id'") || content.contains(botJsonPath)) return

    val botEntry =
      s"""  {
         |    id: '$id',
         |    artist: '$botName',
         |    filePath: '/Dropbox/sBots/$botName/src/main/resources',
         |    jsonFilePath: '../modules/bots/$botName/${id}_list.json',
         |  },""".stripMargin

    val listEnd = "\n];"
    val endIdx  = content.lastIndexOf(listEnd)
    if (endIdx == -1) {
      println(s"Could not find insertion point in $filesCheckConfig")
      return
    }

    val before = content.substring(0, endIdx).stripSuffix("\n")
    val sep    = if (before.trim.endsWith(",")) "" else ","
    content = s"$before$sep\n$botEntry\n];"

    IO.write(filesCheckConfig, content)
    println(s"Updated filesCheck config with $botName ($id)")
  }

  private def copyAndSubstitute(src: File, dest: File, templateRoot: File, botName: String, id: String): Unit = {
    if (src.isDirectory) {
      // When copying the template root (_template), put contents directly in dest instead of creating dest/_template
      val targetDir =
        if (src.getCanonicalFile == templateRoot.getCanonicalFile) dest
        else dest / src.name.replace("TemplateBot", botName).replace("tpl", id)
      targetDir.mkdirs()
      src.listFiles().foreach(f => copyAndSubstitute(f, targetDir, templateRoot, botName, id))
    } else {
      val newName    = src.name.replace("TemplateBot", botName).replace("tpl", id)
      val newFile    = dest / newName
      val content    = IO.read(src)
      val newContent = content.replace("TemplateBot", botName).replace("tpl", id)
      IO.write(newFile, newContent)
    }
  }
}
