package com.benkio.m0sconibot

import cats.effect.*
import com.benkio.telegrambotinfrastructure.dataentry.DataEntry

import java.nio.file.Paths

object M0sconiBotMainDataEntry extends IOApp {

  val mosListFilename     = "mos_list.json"
  val mosListFileResource =
    Resource.make(IO.delay(scala.io.Source.fromFile(mosListFilename)))(bufferedSorce => IO.delay(bufferedSorce.close))

  def mainLogic(args: List[String]): IO[String] =
    DataEntry
      .dataEntryLogic(args, mosListFileResource, Paths.get(mosListFilename))

  def run(args: List[String]): IO[ExitCode] =
    mainLogic(args)
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
