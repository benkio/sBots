package com.benkio.richardphjbensonbot

import cats.effect.*
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource.given
import io.circe.syntax.*

object RichardPHJBensonBotMainDataEntry extends IOApp {
  private def parseInput(links: List[String]): IO[List[MediaFileSource]] =
    links.traverse(MediaFileSource.fromUriString)

  def run(args: List[String]): IO[ExitCode] =
    for
      mediaFiles <- parseInput(args)
      _          <- IO.println(mediaFiles.asJson)
    // TODO:
    // For Json List
    // - Read the rphjb_list.json
    // - Add the mediaFiles json to the array
    // - Write the file

    // For Scala Code Gen
    // - Group by prefix (eg. take the mp3 or mp4)
    // - Convert groups to reply bundle message by content (mix, only audio, only gif)
    // - return the scala code
    yield ExitCode.Success
}
