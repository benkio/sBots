package com.benkio.TemplateBot

import cats.effect.*
import com.benkio.chatcore.SBot
import com.benkio.chatcore.SBotDataEntry

object TemplateBotMainDataEntry extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotDataEntry
      .run(args, SBot.buildSBotConfig(TemplateBot.sBotInfo))
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
