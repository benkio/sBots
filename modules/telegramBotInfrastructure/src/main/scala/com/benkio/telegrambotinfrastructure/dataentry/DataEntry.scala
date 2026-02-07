package com.benkio.telegrambotinfrastructure.dataentry

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.Json

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import fs2.io.file.Path
import scala.io.BufferedSource

object DataEntry {

  private[dataentry] def parseInput(links: List[String]): IO[List[MediaFileSource]] =
    links.traverse(link => MediaFileSource.fromUriString(link.replace("dl=0", "dl=1")))

  def dataEntryLogic(input: List[String], jsonFileResource: Resource[IO, BufferedSource], jsonFilePath: Path) =
    for {
      _ <- IO.println(
        s"[DataEntry:22:42]] Read the input ${input.length} links & parse them to Json"
      )
      mediafileSources <- parseInput(input)
      mediafileSourcesJson = mediafileSources.asJson
      _               <- IO.println("[DataEntry:24:45]] Read the json_list and parse it to json")
      botListFile     <- jsonFileResource.use(_.mkString.pure[IO])
      botListFileJson <- IO.fromEither(parse(botListFile))
      _               <- IO.println("[DataEntry:26:45]] Merge the 2 arrays together")
      mergedArray = {
        // Extract the arrays as lists of Json elements
        val elements1 = botListFileJson.asArray.getOrElse(Vector.empty)
        val elements2 = mediafileSourcesJson.asArray.getOrElse(Vector.empty)
        // Combine and convert back to Json
        Json.fromValues(elements1 ++ elements2)
      }
      _ <- IO.println("[DataEntry] Write the json back")
      _ <- IO(Files.write(jsonFilePath, mergedArray.toString.getBytes(StandardCharsets.UTF_8)))

      _ <- IO.println("[DataEntry] create media file source groups")
      mediaFileSourceGroups = MediaFileSourceGroup.fromMediaFileSourceList(mediafileSources)
      _ <- IO.println("[DataEntry] convert media file source groups to ReplyBundleMessages")
    } yield s"""List(
               |  ${mediaFileSourceGroups.map(MediaFileSourceGroup.toReplyBundleMessageCode).mkString(",\n")}
               |)""".stripMargin
} // end DataEntry
