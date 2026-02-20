package com.benkio.TemplateBot

import cats.effect.*
import com.benkio.telegrambotinfrastructure.SBotMainPolling
import log.effect.LogLevels

object TemplateBotMainPolling extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotMainPolling.run(logLevel = LogLevels.Info, sBotInfo = TemplateBot.sBotInfo)

}
