package com.benkio.ABarberoBot

import cats.effect.*
import com.benkio.telegrambotinfrastructure.SBotMainPolling
import log.effect.LogLevels

object ABarberoBotMainPolling extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotMainPolling.run(logLevel = LogLevels.Info, sBotInfo = ABarberoBot.sBotInfo)

}
