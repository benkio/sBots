package com.benkio.ABarberoBot

import cats.effect.*
import com.benkio.chatcore.SBotDataEntry
import com.benkio.chatcore.config.SBotConfig

object ABarberoBotMainDataEntry extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotDataEntry
      .run(args, SBotConfig.fromSBotInfo(ABarberoBot.sBotInfo))
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
