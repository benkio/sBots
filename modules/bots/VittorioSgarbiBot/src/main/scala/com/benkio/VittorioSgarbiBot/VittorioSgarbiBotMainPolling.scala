package com.benkio.VittorioSgarbiBot

import cats.effect.*
import com.benkio.chattelegramadapter.SBotMainPolling

object VittorioSgarbiBotMainPolling extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotMainPolling.run(sBotInfo = VittorioSgarbiBot.sBotInfo)

}
