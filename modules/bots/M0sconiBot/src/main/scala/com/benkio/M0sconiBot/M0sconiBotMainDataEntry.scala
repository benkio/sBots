package com.benkio.M0sconiBot

import cats.effect.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.SBotDataEntry

object M0sconiBotMainDataEntry extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotDataEntry
      .run(args, SBotConfig.fromSBotInfo(M0sconiBot.sBotInfo))
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
