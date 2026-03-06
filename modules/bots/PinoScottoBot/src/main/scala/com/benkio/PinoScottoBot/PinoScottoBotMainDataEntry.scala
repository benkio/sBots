package com.benkio.PinoScottoBot

import cats.effect.*
import com.benkio.chatcore.SBotDataEntry
import com.benkio.chattelegramadapter.SBot

object PinoScottoBotMainDataEntry extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    SBotDataEntry
      .run(args, SBot.buildSBotConfig(PinoScottoBot.sBotInfo))
      .flatMap(IO.println(_))
      .as(ExitCode.Success)
}
