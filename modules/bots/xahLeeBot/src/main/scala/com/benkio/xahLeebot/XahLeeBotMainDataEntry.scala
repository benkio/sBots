package com.benkio.xahleebot

import cats.effect.*
import com.benkio.telegrambotinfrastructure.dataentry.DataEntry

import java.nio.file.Paths

object XahLeeBotMainDataEntry extends IOApp {

  val xahListFilename     = "xah_list.json"
  val xahListFileResource =
    Resource.make(IO.delay(scala.io.Source.fromFile(xahListFilename)))(bufferedSorce => IO.delay(bufferedSorce.close))

  def mainLogic(args: List[String]): IO[String] =
    DataEntry
      .dataEntryLogic(args, xahListFileResource, Paths.get(xahListFilename))

  def run(args: List[String]): IO[ExitCode] =
    mainLogic(args)
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
