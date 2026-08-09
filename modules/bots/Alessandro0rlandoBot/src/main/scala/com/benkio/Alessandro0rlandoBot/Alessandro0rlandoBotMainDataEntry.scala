package com.benkio.Alessandro0rlandoBot

import cats.effect.*
import com.benkio.chatcore.SBotDataEntry
import com.benkio.chattelegramadapter.SBot

object Alessandro0rlandoBotMainDataEntry extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotDataEntry
      .run(args, SBot.buildSBotConfig(Alessandro0rlandoBot.sBotInfo))
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
