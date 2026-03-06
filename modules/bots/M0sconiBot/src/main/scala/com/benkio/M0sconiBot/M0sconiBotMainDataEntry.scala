package com.benkio.M0sconiBot

import cats.effect.*
import com.benkio.chatcore.SBot
import com.benkio.chatcore.SBotDataEntry

object M0sconiBotMainDataEntry extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotDataEntry
      .run(args, SBot.buildSBotConfig(M0sconiBot.sBotInfo))
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
