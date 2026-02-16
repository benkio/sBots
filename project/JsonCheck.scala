import sbt.*

import java.io.File

object JsonCheck {
  val jsonFiles: Seq[String] = Seq(
    "modules/bots/ABarberoBot/abar_list.json",
    "modules/bots/ABarberoBot/barberoShows.json",
    "modules/bots/CalandroBot/cala_list.json",
    "modules/bots/M0sconiBot/mos_list.json",
    "modules/bots/RichardPHJBensonBot/bensonShows.json",
    "modules/bots/RichardPHJBensonBot/rphjb_list.json",
    "modules/bots/XahLeeBot/xahShows.json",
    "modules/bots/XahLeeBot/xah_list.json",
    "modules/bots/YouTuboAncheI0Bot/youtuboShows.json",
    "modules/bots/YouTuboAncheI0Bot/ytai_list.json",

    "modules/bots/CalandroBot/src/main/resources/cala_replies.json",
    "modules/bots/M0sconiBot/src/main/resources/mos_replies.json",
    "modules/bots/ABarberoBot/src/main/resources/abar_replies.json",
    "modules/bots/YouTuboAncheI0Bot/src/main/resources/ytai_replies.json",
    "modules/bots/RichardPHJBensonBot/src/main/resources/rphjb_replies.json",
    "modules/bots/CalandroBot/src/main/resources/cala_commands.json"
  )

  lazy val checkJsonFilesImpl =
    Def.task {
      import _root_.io.circe.*, _root_.io.circe.parser.*
      import sbt.IO
      import sbt.Keys.streams

      val logger = streams.value.log

      logger.info(s"""Checking JSON files for validity:
                     |${jsonFiles.map(s => "- " + s).mkString("\n")}""".stripMargin)

      jsonFiles.foreach { filePath =>
        val file = new File(filePath)
        if (!file.exists()) {
          logger.error(s"JSON file not found: $filePath")
          sys.error(s"JSON file not found: $filePath")
        } else {
          logger.info(s"Validating $filePath...")
          val content = IO.read(file)
          parse(content) match {
            case Right(_)    => logger.info("✓ valid")
            case Left(error) =>
              logger.error(s"❌ Invalid JSON: ${error.getMessage}")
              sys.error(s"Invalid JSON in $filePath: ${error.getMessage}")
          }
        }
      }

      logger.info("All JSON files are valid")
    }
}
