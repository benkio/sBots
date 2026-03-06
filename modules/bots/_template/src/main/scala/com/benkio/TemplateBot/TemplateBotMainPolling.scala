package com.benkio.TemplateBot

import cats.effect.*
import com.benkio.chattelegramadapter.SBotMainPolling

object TemplateBotMainPolling extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotMainPolling.run(sBotInfo = TemplateBot.sBotInfo)

}
