package com.benkio.chatcore.dataentry

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.media.MediaFileSource
import com.benkio.chatcore.model.SBotInfo.SBotName
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.Json

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object DataEntry {

  private[dataentry] def parseInput(links: List[String]): IO[List[MediaFileSource]] =
    links.traverse(link => MediaFileSource.fromUriString(link.replace("dl=0", "dl=1")))

  private def resolvePath(candidates: List[Path]): IO[Path] =
    candidates
      .find(p => Files.exists(p) && !Files.isDirectory(p))
      .orElse(candidates.headOption)
      .liftTo[IO](new IllegalArgumentException("[DataEntry] No path candidates provided"))

  def dataEntryLogic(input: List[String], sBotConfig: SBotConfig) = {
    for {
      botName          <- IO.pure(sBotConfig.sBotInfo.botName.value)
      listJsonFilepath <- resolvePath(
        List(
          Paths.get(sBotConfig.listJsonFilename),
          Paths.get("modules", "bots", botName, sBotConfig.listJsonFilename)
        )
      )
      repliesJsonFilepath <- resolvePath(
        List(
          Paths.get(sBotConfig.repliesJsonFilename),
          Paths.get("src", "main", "resources", sBotConfig.repliesJsonFilename),
          Paths.get(
            "modules",
            "bots",
            botName,
            "src",
            "main",
            "resources",
            sBotConfig.repliesJsonFilename
          )
        )
      )
      jsonListFileResource = Resource.make(IO.delay(scala.io.Source.fromFile(listJsonFilepath.toFile)))(bufferedSorce =>
        IO.delay(bufferedSorce.close)
      )
      repliesJsonFileResource = Resource.make(IO.delay(scala.io.Source.fromFile(repliesJsonFilepath.toFile)))(
        bufferedSorce => IO.delay(bufferedSorce.close)
      )
      _ <- IO.println(
        s"[DataEntry:22:42]] Read the input ${input.length} links & parse them to Json"
      )
      mediafileSources <- parseInput(input)
      mediafileSourcesJson = mediafileSources.asJson
      _               <- IO.println("[DataEntry:24:45]] Read the json_list and parse it to json")
      botListFile     <- jsonListFileResource.use(_.mkString.pure[IO])
      botListFileJson <- IO.fromEither(parse(botListFile))
      _               <- IO.println("[DataEntry]] Merge the 2 arrays together list")
      mergedArrayList = {
        // Extract the arrays as lists of Json elements
        val elements1 = botListFileJson.asArray.getOrElse(Vector.empty)
        val elements2 = mediafileSourcesJson.asArray.getOrElse(Vector.empty)
        // Combine and convert back to Json
        Json.fromValues(elements1 ++ elements2)
      }
      _ <- IO.println("[DataEntry] Write the json back")
      _ <- IO(Files.write(listJsonFilepath, mergedArrayList.toString.getBytes(StandardCharsets.UTF_8)))

      _ <- IO.println("[DataEntry] create media file source groups")
      mediaFileSourceGroups = MediaFileSourceGroup.fromMediaFileSourceList(mediafileSources)
      _ <- IO.println("[DataEntry] convert media file source groups to ReplyBundleMessages")
      newReplyBundleMessages = mediaFileSourceGroups.map(MediaFileSourceGroup.toReplyBundleMessage).asJson
      _                  <- IO.println("[DataEntry] Read the current replies")
      botRepliesFile     <- repliesJsonFileResource.use(_.mkString.pure[IO])
      botRepliesFileJson <- IO.fromEither(parse(botRepliesFile))
      _                  <- IO.println("[DataEntry]] Merge the 2 arrays together replies")
      mergedArrayReplies = {
        // Extract the arrays as lists of Json elements
        val elements1 = newReplyBundleMessages.asArray.getOrElse(Vector.empty)
        val elements2 = botRepliesFileJson.asArray.getOrElse(Vector.empty)
        // Combine and convert back to Json
        Json.fromValues(elements1 ++ elements2)
      }
      _ <- IO.println("[DataEntry] Write the json back")
      _ <- IO(Files.write(repliesJsonFilepath, mergedArrayReplies.toString.getBytes(StandardCharsets.UTF_8)))
    } yield ()
  }
} // end DataEntry
