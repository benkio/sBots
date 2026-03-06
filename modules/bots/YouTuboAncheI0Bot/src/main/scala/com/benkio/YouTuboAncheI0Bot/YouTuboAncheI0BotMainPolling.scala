package com.benkio.YouTuboAncheI0Bot

import cats.effect.*
import com.benkio.chatcore.SBotMainPolling

object YouTuboAncheI0BotMainPolling extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotMainPolling.run(sBotInfo = YouTuboAncheI0Bot.sBotInfo)

}
