package com.benkio.XahLeeBot

import cats.effect.*
import com.benkio.chatcore.SBotDataEntry
import com.benkio.chattelegramadapter.SBot

object XahLeeBotMainDataEntry extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotDataEntry
      .run(args, SBot.buildSBotConfig(XahLeeBot.sBotInfo))
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
