package com.benkio.calandrobot

import cats.effect.*
import log.effect.LogLevels

object CalandroBotMainPolling extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotMainPolling.run(logLevel = LogLevels.Info, sBotInfo = CalandroBot.sBotInfo)

}
