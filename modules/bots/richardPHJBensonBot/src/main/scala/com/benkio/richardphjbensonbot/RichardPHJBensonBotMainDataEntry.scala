package com.benkio.richardphjbensonbot

import com.benkio.telegrambotinfrastructure.model.MimeType
import cats.effect.*
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource.given
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.Json

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

object RichardPHJBensonBotMainDataEntry extends IOApp {

  val rphjbListFilename = "rphjb_list.json"
  val rphjbListFileResource =
    Resource.make(IO.delay(scala.io.Source.fromFile(rphjbListFilename)))(bufferedSorce => IO.delay(bufferedSorce.close))

  case class MediaFileSourceGroup(
    gif: Option[MediaFileSource] = None,
    mp3: Option[MediaFileSource] = None,
    vid: Option[MediaFileSource] = None
  )

  private def isGifMp4(mediaFileName: String): Boolean =
    mediaFileName.dropRight(4).takeRight(3).toLowerCase == "gif"
  private def getPrefix(mediaFileName: String): String =
    if isGifMp4(mediaFileName)
    then mediaFileName.dropRight(7)
    else mediaFileName.dropRight(4)

  private def toMediaFileSourceGroup(mediaFileSources: List[MediaFileSource]): List[MediaFileSourceGroup] =
    mediaFileSources.groupBy(x => getPrefix(x.filename)).map { case (_, mediaFileSources: List[MediaFileSource]) =>
      mediaFileSources.foldLeft(MediaFileSourceGroup()) {
        case (mediaFileSourceGroup:MediaFileSourceGroup, mediaFileSource: MediaFileSource) =>
          mediaFileSourceGroup.copy(
            gif = if mediaFileSource.mime == MimeType.GIF then Some(mediaFileSource) else mediaFileSourceGroup.gif,
            mp3 = if mediaFileSource.mime == MimeType.MPEG then Some(mediaFileSource) else mediaFileSourceGroup.mp3,
            vid = if mediaFileSource.mime == MimeType.MP4 then Some(mediaFileSource) else  mediaFileSourceGroup.vid,
          )
      }
    }.toList

  private def parseInput(links: List[String]): IO[List[MediaFileSource]] =
    links.traverse(link => MediaFileSource.fromUriString(link.replace("dl=0", "dl=1")))

  def run(args: List[String]): IO[ExitCode] =
    for
      _ <- IO.println(
        s"[RichardPHJBensonBotMainDataEntry:22:42]] Read the input ${args.length} links & parse them to Json"
      )
      mediafileSources     <- parseInput(args)
      mediafileSourcesJson = mediafileSources.asJson
      _             <- IO.println("[RichardPHJBensonBotMainDataEntry:24:45]] Read the rphjb_list and parse it to json")
      rphjbListFile <- rphjbListFileResource.use(_.mkString.pure[IO])
      rphjbListFileJson <- IO.fromEither(parse(rphjbListFile))
      _                 <- IO.println("[RichardPHJBensonBotMainDataEntry:26:45]] Merge the 2 arrays together")
      mergedArray = {
        // Extract the arrays as lists of Json elements
        val elements1 = rphjbListFileJson.asArray.getOrElse(Vector.empty)
        val elements2 = mediafileSourcesJson.asArray.getOrElse(Vector.empty)
        // Combine and convert back to Json
        Json.fromValues(elements1 ++ elements2)
      }
      _ <- IO.println("[RichardPHJBensonBotMainDataEntry:34:45]] Write the json back")
      _ <- IO(Files.write(Paths.get(rphjbListFilename), mergedArray.toString.getBytes(StandardCharsets.UTF_8)))

      mediaFileSourceGroups = toMediaFileSourceGroup(mediafileSources)
      _ <- IO.println(s"[RichardPHJBensonBotMainDataEntry:71:45]] groups: $mediaFileSourceGroups")
    // For Scala Code Gen
    // - Convert groups to reply bundle message by content (mix, only audio, only gif)
    // - return the scala code
    yield ExitCode.Success
}
