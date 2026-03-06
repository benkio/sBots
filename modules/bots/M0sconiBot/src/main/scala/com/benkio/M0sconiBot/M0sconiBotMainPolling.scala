package com.benkio.M0sconiBot

import cats.effect.*
import com.benkio.chattelegramadapter.SBotMainPolling

object M0sconiBotMainPolling extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotMainPolling.run(sBotInfo = M0sconiBot.sBotInfo)

}
