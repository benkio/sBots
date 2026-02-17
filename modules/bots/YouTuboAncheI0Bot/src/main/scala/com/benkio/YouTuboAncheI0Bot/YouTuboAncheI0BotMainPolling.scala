package com.benkio.YouTuboAncheI0Bot

import cats.effect.*
import com.benkio.telegrambotinfrastructure.SBotMainPolling
import log.effect.LogLevels

object YouTuboAncheI0BotMainPolling extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotMainPolling.run(logLevel = LogLevels.Info, sBotInfo = YouTuboAncheI0Bot.sBotInfo)

}
