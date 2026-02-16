package com.benkio.YouTuboAncheI0Bot

import cats.effect.*
import com.benkio.telegrambotinfrastructure.dataentry.DataEntry

import java.nio.file.Paths

object YouTuboAncheI0BotMainDataEntry extends IOApp {

  val ytaiListFilename     = "ytai_list.json"
  val ytaiListFileResource =
    Resource.make(IO.delay(scala.io.Source.fromFile(ytaiListFilename)))(bufferedSorce => IO.delay(bufferedSorce.close))
  def mainLogic(args: List[String]): IO[String] =
    DataEntry
      .dataEntryLogic(args, ytaiListFileResource, Paths.get(ytaiListFilename))

  def run(args: List[String]): IO[ExitCode] =
    mainLogic(args)
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
