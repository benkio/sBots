package com.benkio.TemplateBot

import cats.effect.*
import com.benkio.chatcore.SBotDataEntry
import com.benkio.chattelegramadapter.SBot

object TemplateBotMainDataEntry extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotDataEntry
      .run(args, SBot.buildSBotConfig(TemplateBot.sBotInfo))
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
