package com.benkio.chatcore

import cats.effect.*
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Logger.given
import com.benkio.chatcore.SBot
import com.benkio.chatcore.SBotPolling

object SBotMainPolling {

  def run(sBotInfo: SBotInfo): cats.effect.IO[cats.effect.ExitCode] = {
    SBot
      .buildPollingBot((cb: SBotPolling[IO]) => cb.start(), sBotInfo)
      .as(ExitCode.Success)
  }

}
