package com.benkio.Alessandro0rlandoBot

import cats.effect.*
import com.benkio.chattelegramadapter.SBotMainPolling

object Alessandro0rlandoBotMainPolling extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotMainPolling.run(sBotInfo = Alessandro0rlandoBot.sBotInfo)

}
