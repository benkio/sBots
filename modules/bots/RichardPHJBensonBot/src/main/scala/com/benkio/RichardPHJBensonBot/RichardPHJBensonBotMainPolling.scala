package com.benkio.RichardPHJBensonBot

import cats.effect.*
import com.benkio.telegrambotinfrastructure.SBotMainPolling
import log.effect.LogLevels

object RichardPHJBensonBotMainPolling extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotMainPolling.run(logLevel = LogLevels.Info, sBotInfo = RichardPHJBensonBot.sBotInfo)

}
