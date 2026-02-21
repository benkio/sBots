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
    addBotToDeployWorkflow(base, botName, id)
    println(s"Created bot module at $targetDir")
    println(
      s"Next: define the project in build.sbt, add to BotsRegistry (or run: ./scripts/CompleteBotRegistration.sc $botName $id). See docs/adding-a-bot.md"
    )
  }

  private def addBotToBotDBApplicationConf(base: File, botName: String, id: String): Unit = {
    val appConf = base / "modules" / "botDB" / "src" / "main" / "resources" / "application.conf"
    if (!appConf.isFile) return
    var content = IO.read(appConf)
    if (content.contains(s"""bot-id = "$id"""")) return // already added
    content = content.replace(
      """{ bot-id = "ytai", value = "../../../../bots/YouTuboAncheI0Bot"},""",
      s"""{ bot-id = "ytai", value = "../../../../bots/YouTuboAncheI0Bot"},
         |        { bot-id = "$id", value = "../../../../bots/$botName"},""".stripMargin
    )
    content = content.replace(
      """            {
        |                bot-id        = "ytai",
        |                caption-language = "it",
        |                youtube-sources = ["@youtuboancheio1365"],
        |                output-file-path = "../bots/youTuboAncheI0Bot/ytai_shows.json"
        |            },""".stripMargin,
      """            {
        |                bot-id        = "ytai",
        |                caption-language = "it",
        |                youtube-sources = ["@youtuboancheio1365"],
        |                output-file-path = "../bots/youTuboAncheI0Bot/ytai_shows.json"
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

  private def copyAndSubstitute(src: File, dest: File, templateRoot: File, botName: String, id: String): Unit = {
    if (src.isDirectory) {
      val newName = src.name.replace("TemplateBot", botName).replace("tpl", id)
      val newDir  = dest / newName
      newDir.mkdirs()
      src.listFiles().foreach(f => copyAndSubstitute(f, newDir, templateRoot, botName, id))
    } else {
      val newName    = src.name.replace("TemplateBot", botName).replace("tpl", id)
      val newFile    = dest / newName
      val content    = IO.read(src)
      val newContent = content.replace("TemplateBot", botName).replace("tpl", id)
      IO.write(newFile, newContent)
    }
  }
}
