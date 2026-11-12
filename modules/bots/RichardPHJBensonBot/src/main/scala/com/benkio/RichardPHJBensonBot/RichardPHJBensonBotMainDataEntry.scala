package com.benkio.RichardPHJBensonBot

import cats.effect.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.SBotDataEntry

object RichardPHJBensonBotMainDataEntry extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotDataEntry
      .run(args, SBotConfig.fromSBotInfo(RichardPHJBensonBot.sBotInfo))
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
