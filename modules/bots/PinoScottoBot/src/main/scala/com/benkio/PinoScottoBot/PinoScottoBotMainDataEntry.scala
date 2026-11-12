package com.benkio.PinoScottoBot

import cats.effect.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.SBotDataEntry

object PinoScottoBotMainDataEntry extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotDataEntry
      .run(args, SBotConfig.fromSBotInfo(PinoScottoBot.sBotInfo))
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
