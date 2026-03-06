package com.benkio.chattelegramadapter

import cats.effect.*
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Logger.given

object SBotMainPolling {

  def run(sBotInfo: SBotInfo): cats.effect.IO[cats.effect.ExitCode] = {
    SBot
      .buildPollingBot((cb: SBotPolling[IO]) => cb.start(), sBotInfo)
      .as(ExitCode.Success)
  }

}
