package com.benkio.richardphjbensonbot

import cats.effect.*
import com.benkio.telegrambotinfrastructure.dataentry.DataEntry

import java.nio.file.Paths

object RichardPHJBensonBotMainDataEntry extends IOApp {

  val rphjbListFilename = "rphjb_list.json"
  val rphjbListFileResource =
    Resource.make(IO.delay(scala.io.Source.fromFile(rphjbListFilename)))(bufferedSorce => IO.delay(bufferedSorce.close))

  def run(args: List[String]): IO[ExitCode] =
    DataEntry
      .dataEntryLogic(args, rphjbListFileResource, Paths.get(rphjbListFilename))
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
