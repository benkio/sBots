package com.benkio.XahLeeBot

import cats.effect.*
import com.benkio.chatcore.SBot
import com.benkio.chatcore.SBotDataEntry

object XahLeeBotMainDataEntry extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotDataEntry
      .run(args, SBot.buildSBotConfig(XahLeeBot.sBotInfo))
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
