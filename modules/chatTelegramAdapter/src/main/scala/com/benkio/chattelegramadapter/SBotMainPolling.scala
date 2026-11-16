package com.benkio.chattelegramadapter

import cats.effect.*
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Logger.given
import com.benkio.chattelegramadapter.http.TelegramNotifier

object SBotMainPolling {

  def run(
      sBotInfo: SBotInfo,
      opsChatId: Long = TelegramNotifier.defaultOpsChatId
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
