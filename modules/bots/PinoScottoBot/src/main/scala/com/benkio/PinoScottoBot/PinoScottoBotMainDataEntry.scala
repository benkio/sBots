package com.benkio.PinoScottoBot

import cats.effect.*
import com.benkio.telegrambotinfrastructure.SBot
import com.benkio.telegrambotinfrastructure.SBotDataEntry

object PinoScottoBotMainDataEntry extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotDataEntry
      .run(args, SBot.buildSBotConfig(PinoScottoBot.sBotInfo))
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
