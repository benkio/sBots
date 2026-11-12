package com.benkio.TemplateBot

import cats.effect.*
import com.benkio.chatcore.SBotDataEntry
import com.benkio.chatcore.config.SBotConfig

object TemplateBotMainDataEntry extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotDataEntry
      .run(args, SBotConfig.fromSBotInfo(TemplateBot.sBotInfo))
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
