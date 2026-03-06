package com.benkio.RichardPHJBensonBot

import cats.effect.*
import com.benkio.chatcore.SBotMainPolling

object RichardPHJBensonBotMainPolling extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotMainPolling.run(sBotInfo = RichardPHJBensonBot.sBotInfo)

}
