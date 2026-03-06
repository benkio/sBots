package com.benkio.ABarberoBot

import cats.effect.*
import com.benkio.chatcore.SBotDataEntry
import com.benkio.chattelegramadapter.SBot

object ABarberoBotMainDataEntry extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotDataEntry
      .run(args, SBot.buildSBotConfig(ABarberoBot.sBotInfo))
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
