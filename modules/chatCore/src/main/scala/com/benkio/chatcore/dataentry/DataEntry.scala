package com.benkio.chatcore.dataentry

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.media.MediaFileSource
import com.benkio.chatcore.model.media.MediaSource
import com.benkio.chatcore.model.MimeTypeOps
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.Json
import org.http4s.Uri

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

object DataEntry {

  private val FilenameUrlDelimiter = "="
  private val DropboxDownloadFlag  = "([?&])dl=0(?=&|#|$)".r

  // The `dl=0` -> `dl=1` rewrite only applies to Dropbox links
  private def normalizeUrl(url: String): String =
    Uri.fromString(url) match {
      case Right(uri) if uri.host.exists(_.value.contains("dropbox.com")) =>
        DropboxDownloadFlag.replaceAllIn(url, "$1dl=1")
      case _ => url
    }

  private def validateFilename(botId: String, filename: String): IO[Unit] =
    IO.raiseUnless(filename.startsWith(s"${botId}_") && filename.length > botId.length + 1)(
      Throwable(s"[DataEntry] filename must have the form '${botId}_<name>': $filename")
    )

  private def parseInputLink(botId: String)(link: String): IO[MediaFileSource] = {
    val trimmedInput = link.trim
    val isPlainUrl   = trimmedInput.startsWith("http://") || trimmedInput.startsWith("https://")

    if isPlainUrl then for {
      source <- MediaFileSource.fromUriString[IO](normalizeUrl(trimmedInput))
      _      <- validateFilename(botId, source.filename)
    } yield source
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
          _   <- validateFilename(botId, filename)
          uri <- IO.fromEither(Uri.fromString(normalizeUrl(url)))
        } yield MediaFileSource(
          filename = filename,
          kinds = List.empty,
          mime = MimeTypeOps.mimeTypeOrDefault(filename, None),
          sources = List(MediaSource.Url(uri))
        )
      case _ =>
        IO.raiseError(
          Throwable(s"[DataEntry] Invalid input format, expected '${botId}_name${FilenameUrlDelimiter}url': $link")
        )
    }
    else
      IO.raiseError(
        Throwable(
          s"[DataEntry] Input must be either a plain URL or '${botId}_name${FilenameUrlDelimiter}url': $link"
        )
      )
  }

  private[dataentry] def parseInput(botId: String, links: List[String]): IO[List[MediaFileSource]] =
    links.traverse(parseInputLink(botId))

  // A file that is not valid json, or not a json array, is rejected
  private def parseArray(path: Path, content: String): IO[Vector[Json]] =
    for {
      json  <- IO.fromEither(parse(content))
      array <- IO.fromOption(json.asArray)(Throwable(s"[DataEntry] Not a json array: ${path.toAbsolutePath}"))
    } yield array

  // Write to a sibling temp file, then move it over the target
  private def writeAtomically(path: Path, content: String): IO[Unit] =
    IO.blocking {
      val tmp = Files.createTempFile(path.toAbsolutePath.getParent, path.getFileName.toString, ".tmp")
      Files.write(tmp, content.getBytes(StandardCharsets.UTF_8))
      Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING)
      ()
    }

  private def readFile(path: Path): IO[String] =
    Resource
      .make(IO.delay(scala.io.Source.fromFile(path.toFile)))(bufferedSource => IO.delay(bufferedSource.close))
      .use(_.mkString.pure[IO])

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
      _ <- IO.println(
        s"[DataEntry:22:42]] Read the input ${input.length} links & parse them to Json"
      )
      mediafileSources <- parseInput(sBotConfig.sBotInfo.botId.value, input)
      mediafileSourcesJson = mediafileSources.asJson
      _               <- IO.println("[DataEntry:24:45]] Read the json_list and parse it to json")
      botListFile     <- readFile(listJsonFilepath)
      botListElements <- parseArray(listJsonFilepath, botListFile)
      _               <- IO.println("[DataEntry]] Merge the 2 arrays together list")
      mergedArrayList = {
        // Extract the arrays as lists of Json elements
        val elements2 = mediafileSourcesJson.asArray.getOrElse(Vector.empty)
        // Combine and convert back to Json
        Json.fromValues(botListElements ++ elements2)
      }

      _ <- IO.println("[DataEntry] create media file source groups")
      mediaFileSourceGroups = MediaFileSourceGroup.fromMediaFileSourceList(mediafileSources)
      _ <- IO.println("[DataEntry] convert media file source groups to ReplyBundleMessages")
      newReplyBundleMessages = mediaFileSourceGroups.map(MediaFileSourceGroup.toReplyBundleMessage).asJson
      _                  <- IO.println("[DataEntry] Read the current replies")
      botRepliesFile     <- readFile(repliesJsonFilepath)
      botRepliesElements <- parseArray(repliesJsonFilepath, botRepliesFile)
      _                  <- IO.println("[DataEntry]] Merge the 2 arrays together replies")
      mergedArrayReplies = {
        // Extract the arrays as lists of Json elements
        val elements1 = newReplyBundleMessages.asArray.getOrElse(Vector.empty)
        // Combine and convert back to Json
        Json.fromValues(elements1 ++ botRepliesElements)
      }

      // Everything that can fail is done: write the files only now, so an invalid file leaves both untouched
      _ <- IO.println("[DataEntry] Write the jsons back")
      _ <- writeAtomically(listJsonFilepath, mergedArrayList.toString)
      // if the replies write fails, restore the list so that the two files stay consistent
      _ <- writeAtomically(repliesJsonFilepath, mergedArrayReplies.toString).onError(_ =>
        writeAtomically(listJsonFilepath, botListFile).attempt.void
      )
    } yield ()
  }
} // end DataEntry
