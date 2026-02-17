package com.benkio.CalandroBot

import cats.effect.*
import com.benkio.telegrambotinfrastructure.SBotMainPolling
import log.effect.LogLevels

object CalandroBotMainPolling extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotMainPolling.run(logLevel = LogLevels.Info, sBotInfo = CalandroBot.sBotInfo)

}
