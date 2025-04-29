package com.benkio.richardphjbensonbot

import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import cats.effect.*
import cats.syntax.all.*

object RichardPHJBensonBotMainDataEntry extends IOApp {
  private def parseInput(links: List[String]): IO[List[MediaFile]] =
    links.traverse(MediaFile.fromString)

  def run(args: List[String]): IO[ExitCode] =
    for
      mediaFiles <- parseInput(args)
      _ <- IO.println(mediaFiles.map(_.show))
    yield ExitCode.Success
}
