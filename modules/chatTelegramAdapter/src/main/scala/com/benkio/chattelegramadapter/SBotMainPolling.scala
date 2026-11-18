package com.benkio.chattelegramadapter

import cats.effect.*
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Logger.given

object SBotMainPolling {

  def run(
      sBotInfo: SBotInfo,
      opsChatId: Long
  ): cats.effect.IO[cats.effect.ExitCode] = {
    SBot
      .buildPollingBot(
        (cb: SBotPolling[IO]) => {
          TelegramStartupCheck.run(cb.sBotSetup, "Start Polling Bot Successful ✅") >>
            cb.start()
        },
        sBotInfo,
        opsChatId = opsChatId
      )
      .as(ExitCode.Success)
  }

}
