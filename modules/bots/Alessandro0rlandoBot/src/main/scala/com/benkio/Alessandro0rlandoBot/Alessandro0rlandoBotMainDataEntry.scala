package com.benkio.Alessandro0rlandoBot

import cats.effect.*
import com.benkio.chatcore.SBotDataEntry
import com.benkio.chatcore.config.SBotConfig

object Alessandro0rlandoBotMainDataEntry extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotDataEntry
      .run(args, SBotConfig.fromSBotInfo(Alessandro0rlandoBot.sBotInfo))
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
