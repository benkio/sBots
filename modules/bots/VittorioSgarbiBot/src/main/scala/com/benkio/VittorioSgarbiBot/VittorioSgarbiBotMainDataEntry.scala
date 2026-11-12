package com.benkio.VittorioSgarbiBot

import cats.effect.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.SBotDataEntry

object VittorioSgarbiBotMainDataEntry extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotDataEntry
      .run(args, SBotConfig.fromSBotInfo(VittorioSgarbiBot.sBotInfo))
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
