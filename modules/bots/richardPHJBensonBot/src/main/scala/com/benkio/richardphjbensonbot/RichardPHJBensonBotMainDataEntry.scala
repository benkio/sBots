package com.benkio.richardphjbensonbot

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

  private def parseInput(links: List[String]): IO[List[MediaFileSource]] =
    links.traverse(MediaFileSource.fromUriString)

  def run(args: List[String]): IO[ExitCode] =
    for
      _ <- IO.println(
        s"[RichardPHJBensonBotMainDataEntry:22:42]] Read the input ${args.length} links & parse them to Json"
      )
      mediaFiles    <- parseInput(args).map(_.asJson)
      _             <- IO.println("[RichardPHJBensonBotMainDataEntry:24:45]] Read the rphjb_list and parse it to json")
      rphjbListFile <- rphjbListFileResource.use(_.mkString.pure[IO])
      rphjbListFileJson <- IO.fromEither(parse(rphjbListFile))
      _                 <- IO.println("[RichardPHJBensonBotMainDataEntry:26:45]] Merge the 2 arrays together")
      mergedArray = {
        // Extract the arrays as lists of Json elements
        val elements1 = rphjbListFileJson.asArray.getOrElse(Vector.empty)
        val elements2 = mediaFiles.asArray.getOrElse(Vector.empty)
        // Combine and convert back to Json
        Json.fromValues(elements1 ++ elements2)
      }
      _ <- IO.println("[RichardPHJBensonBotMainDataEntry:34:45]] Write the json back")
      _ <- IO(Files.write(Paths.get(rphjbListFilename), mergedArray.toString.getBytes(StandardCharsets.UTF_8)))

    // For Scala Code Gen
    // - Group by prefix (eg. take the mp3 or mp4)
    // - Convert groups to reply bundle message by content (mix, only audio, only gif)
    // - return the scala code
    yield ExitCode.Success
}
