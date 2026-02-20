import sbt.*
import sbt.io.{IO, Path}
import java.io.File

object NewBotTask {

  /** Copies the bot template from modules/bots/_template to modules/bots/botName
    * and replaces TemplateBot -> botName, tpl -> id in paths and file contents.
    */
  def createFromTemplate(base: File, botName: String, id: String): Unit = {
    val templateDir = base / "modules" / "bots" / "_template"
    val targetDir  = base / "modules" / "bots" / botName

    if (!templateDir.exists())
      throw new MessageOnlyException(s"Template not found: $templateDir")
    if (targetDir.exists())
      throw new MessageOnlyException(s"Target already exists: $targetDir")

    copyAndSubstitute(templateDir, targetDir, templateDir, botName, id)
    println(s"Created bot module at $targetDir")
    println(s"Next: define the project in build.sbt, add to BotsRegistry (or run: ./scripts/CompleteBotRegistration.sc $botName $id). See docs/adding-a-bot.md")
  }

  private def copyAndSubstitute(src: File, dest: File, templateRoot: File, botName: String, id: String): Unit = {
    if (src.isDirectory) {
      val newName = src.name.replace("TemplateBot", botName).replace("tpl", id)
      val newDir  = dest / newName
      newDir.mkdirs()
      src.listFiles().foreach(f => copyAndSubstitute(f, newDir, templateRoot, botName, id))
    } else {
      val newName = src.name.replace("TemplateBot", botName).replace("tpl", id)
      val newFile = dest / newName
      val content = IO.read(src)
      val newContent = content.replace("TemplateBot", botName).replace("tpl", id)
      IO.write(newFile, newContent)
    }
  }
}
