package com.benkio.YouTuboAncheI0Bot

import cats.effect.*
import com.benkio.telegrambotinfrastructure.SBot
import com.benkio.telegrambotinfrastructure.SBotDataEntry

object YouTuboAncheI0BotMainDataEntry extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotDataEntry
      .run(args, SBot.buildSBotConfig(YouTuboAncheI0Bot.sBotInfo))
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
