package com.benkio.telegrambotinfrastructure

import cats.effect.*
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.dataentry.DataEntry

import java.nio.file.Paths

object SBotDataEntry {

  def run(args: List[String], sBotConfig: SBotConfig): IO[String] = {
    val abarListFileResource =
      Resource.make(IO.delay(scala.io.Source.fromFile(sBotConfig.listJsonFilename)))(bufferedSorce =>
        IO.delay(bufferedSorce.close)
      )

    DataEntry
      .dataEntryLogic(args, abarListFileResource, Paths.get(sBotConfig.listJsonFilename))
  }

}
