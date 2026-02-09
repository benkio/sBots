import sbt.*

import java.io.File

object JsonCheck {
  val jsonFiles: Seq[String] = Seq(
    "modules/bots/aBarberoBot/abar_list.json",
    "modules/bots/aBarberoBot/barberoShows.json",
    "modules/bots/calandroBot/cala_list.json",
    "modules/bots/m0sconiBot/mos_list.json",
    "modules/bots/richardPHJBensonBot/bensonShows.json",
    "modules/bots/richardPHJBensonBot/rphjb_list.json",
    "modules/bots/xahLeeBot/xahShows.json",
    "modules/bots/xahLeeBot/xah_list.json",
    "modules/bots/youTuboAncheI0Bot/youtuboShows.json",
    "modules/bots/youTuboAncheI0Bot/ytai_list.json",
    "modules/bots/calandroBot/src/main/resources/cala_replies.json",
    "modules/bots/m0sconiBot/src/main/resources/mos_replies.json",
    "modules/bots/aBarberoBot/src/main/resources/abar_replies.json",
    "modules/bots/youTuboAncheI0Bot/src/main/resources/ytai_replies.json"
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
