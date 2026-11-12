package com.benkio.XahLeeBot

import cats.effect.*
import com.benkio.chatcore.SBotDataEntry
import com.benkio.chatcore.config.SBotConfig

object XahLeeBotMainDataEntry extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotDataEntry
      .run(args, SBotConfig.fromSBotInfo(XahLeeBot.sBotInfo))
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
