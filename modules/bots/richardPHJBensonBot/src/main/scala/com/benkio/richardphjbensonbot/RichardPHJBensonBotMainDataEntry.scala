package com.benkio.richardphjbensonbot

import cats.effect.*
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource.given
import com.benkio.telegrambotinfrastructure.model.MimeType
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
      mediaFileSources: List[MediaFileSource]
  )

  private def isGifMp4(mediaFileName: String): Boolean =
    mediaFileName.dropRight(4).takeRight(3).toLowerCase == "gif"
  private def getPrefix(mediaFileName: String): String =
    if isGifMp4(mediaFileName)
    then mediaFileName.dropRight(7)
    else mediaFileName.dropRight(4)

  private def toMediaFileSourceGroup(mediaFileSources: List[MediaFileSource]): List[MediaFileSourceGroup] =
    mediaFileSources
      .groupBy(x => getPrefix(x.filename))
      .map { case (_, mediaFileSources: List[MediaFileSource]) =>
        MediaFileSourceGroup(mediaFileSources)
      }
      .toList

  private def toReplyBundleMessage(mediaFileSourceGroup: MediaFileSourceGroup): String =
    def toMediaFiles(mfs: List[MediaFileSource]): String = mfs
      .map(mfs =>
        val stringContext = mfs.mime match
          case MimeType.GIF                 => "gif"
          case MimeType.JPEG | MimeType.PNG => "pho"
          case MimeType.STICKER             => "sticker"
          case MimeType.MPEG                => "mp3"
          case MimeType.MP4                 => "vid"
          case MimeType.DOC                 => "doc"
        s"""$stringContext"${mfs.filename}""""
      )
      .mkString(",\n    ")
    val replyBundleMessageMethod: String = mediaFileSourceGroup match
      case MediaFileSourceGroup(mediaFileSources) if mediaFileSources.forall(_.mime == MimeType.GIF) =>
        "textToGif"
      case MediaFileSourceGroup(mediaFileSources) if mediaFileSources.forall(_.mime == MimeType.MPEG) =>
        "textToMp3"
      case MediaFileSourceGroup(mediaFileSources) if mediaFileSources.forall(_.mime == MimeType.MP4) =>
        "textToVideo"
      case MediaFileSourceGroup(mediaFileSources) => "textToMedia"
    s"""ReplyBundleMessage
       |  .$replyBundleMessageMethod[F](
       |    ""
       |  )(
       |    ${toMediaFiles(mediaFileSourceGroup.mediaFileSources)}
       |  )""".stripMargin

  private def parseInput(links: List[String]): IO[List[MediaFileSource]] =
    links.traverse(link => MediaFileSource.fromUriString(link.replace("dl=0", "dl=1")))

  def run(args: List[String]): IO[ExitCode] =
    for
      _ <- IO.println(
        s"[RichardPHJBensonBotMainDataEntry:22:42]] Read the input ${args.length} links & parse them to Json"
      )
      mediafileSources <- parseInput(args)
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
      _ <- IO.println("[RichardPHJBensonBotMainDataEntry] Write the json back")
      _ <- IO(Files.write(Paths.get(rphjbListFilename), mergedArray.toString.getBytes(StandardCharsets.UTF_8)))

      _ <- IO.println("[RichardPHJBensonBotMainDataEntry] create media file source groups")
      mediaFileSourceGroups = toMediaFileSourceGroup(mediafileSources)
      _ <- IO.println("[RichardPHJBensonBotMainDataEntry] convert media file source groups to ReplyBundleMessages")
      replyBundleMessages = s"""List(
                               |  ${mediaFileSourceGroups.map(toReplyBundleMessage).mkString(",\n")}
                               |)""".stripMargin
      _ <- IO(println(replyBundleMessages))
    yield ExitCode.Success
}
