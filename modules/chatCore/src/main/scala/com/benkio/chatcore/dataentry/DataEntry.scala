package com.benkio.chatcore.dataentry

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.media.MediaFileSource
import com.benkio.chatcore.model.MimeTypeOps
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.Json
import org.http4s.Uri

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

object DataEntry {

  private val FilenameUrlDelimiter = "="

  private def normalizeUrl(url: String): String = url.replace("dl=0", "dl=1")

  private def parseInputLink(link: String): IO[MediaFileSource] = {
    val trimmedInput = link.trim
    val isPlainUrl   = trimmedInput.startsWith("http://") || trimmedInput.startsWith("https://")

    if isPlainUrl then MediaFileSource.fromUriString(normalizeUrl(trimmedInput))
    else if trimmedInput.contains(FilenameUrlDelimiter) then trimmedInput.split(FilenameUrlDelimiter, 2).toList match {
      case filenameRaw :: urlRaw :: Nil =>
        val filename = filenameRaw.trim
        val url      = urlRaw.trim
        for {
          _ <- IO.raiseUnless(filename.nonEmpty)(
            Throwable(s"[DataEntry] Missing filename before '$FilenameUrlDelimiter' in input: $link")
          )
          _ <- IO.raiseUnless(url.nonEmpty)(
            Throwable(s"[DataEntry] Missing url after '$FilenameUrlDelimiter' in input: $link")
          )
          _ <- IO.raiseUnless(filename.contains('_'))(
            Throwable(
              s"[DataEntry] filename does not contain '_' separator between botId and actual name: $filename"
            )
          )
          uri <- IO.fromEither(Uri.fromString(normalizeUrl(url)))
        } yield MediaFileSource(
          filename = filename,
          kinds = List.empty,
          mime = MimeTypeOps.mimeTypeOrDefault(filename, None),
          sources = List(Right(uri))
        )
      case _ =>
        IO.raiseError(
          Throwable(s"[DataEntry] Invalid input format, expected 'filename${FilenameUrlDelimiter}url': $link")
        )
    }
    else
      IO.raiseError(
        Throwable(
          s"[DataEntry] Input must be either a plain URL or 'filename${FilenameUrlDelimiter}url': $link"
        )
      )
  }

  private[dataentry] def parseInput(links: List[String]): IO[List[MediaFileSource]] =
    links.traverse(parseInputLink)

  def dataEntryLogic(input: List[String], sBotConfig: SBotConfig): IO[Unit] = {
    for {
      listJsonFilepath    <- IO.pure(sBotConfig.listJsonFilePath)
      repliesJsonFilepath <- IO.pure(sBotConfig.repliesJsonFilePath)
      _                   <- IO.raiseUnless(Files.exists(listJsonFilepath))(
        Throwable(s"[DataEntry] Missing list json file at: ${listJsonFilepath.toAbsolutePath}")
      )
      _ <- IO.raiseUnless(Files.exists(repliesJsonFilepath))(
        Throwable(s"[DataEntry] Missing replies json file at: ${repliesJsonFilepath.toAbsolutePath}")
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
