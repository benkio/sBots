package com.benkio.chatcore

import cats.effect.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.dataentry.DataEntry

object SBotDataEntry {

  def run(args: List[String], sBotConfig: SBotConfig): IO[Unit] =
    DataEntry
      .dataEntryLogic(args, sBotConfig)

}
