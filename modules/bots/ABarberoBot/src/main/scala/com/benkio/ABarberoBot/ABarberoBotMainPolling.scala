package com.benkio.ABarberoBot

import cats.effect.*
import com.benkio.chattelegramadapter.SBotMainPolling

object ABarberoBotMainPolling extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotMainPolling.run(sBotInfo = ABarberoBot.sBotInfo)

}
