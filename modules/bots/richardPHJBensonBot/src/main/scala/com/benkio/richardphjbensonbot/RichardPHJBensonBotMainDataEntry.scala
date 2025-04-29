package com.benkio.richardphjbensonbot

import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource
import cats.effect.*
import cats.syntax.all.*

object RichardPHJBensonBotMainDataEntry extends IOApp {
  private def parseInput(links: List[String]): IO[List[MediaFileSource]] =
    links.traverse(MediaFileSource.fromUriString)

  def run(args: List[String]): IO[ExitCode] =
    for
      mediaFiles <- parseInput(args)
      _          <- IO.println(mediaFiles.map(_.toString))
    yield ExitCode.Success
}
