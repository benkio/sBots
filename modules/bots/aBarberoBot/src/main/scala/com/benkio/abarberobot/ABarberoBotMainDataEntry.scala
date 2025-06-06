package com.benkio.abarberobot

import cats.effect.*
import com.benkio.telegrambotinfrastructure.dataentry.DataEntry

import java.nio.file.Paths

object ABarberoBotMainDataEntry extends IOApp {

  val abarListFilename     = "abar_list.json"
  val abarListFileResource =
    Resource.make(IO.delay(scala.io.Source.fromFile(abarListFilename)))(bufferedSorce => IO.delay(bufferedSorce.close))

  def mainLogic(args: List[String]): IO[String] =
    DataEntry
      .dataEntryLogic(args, abarListFileResource, Paths.get(abarListFilename))

  def run(args: List[String]): IO[ExitCode] =
    mainLogic(args)
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
