package com.benkio.chattelegramadapter

import cats.effect.*
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Logger.given
import com.benkio.chattelegramadapter.http.LogTelegramChat
import telegramium.bots.high.Api

object SBotMainPolling {

  def run(sBotInfo: SBotInfo): cats.effect.IO[cats.effect.ExitCode] = {
    SBot
      .buildPollingBot(
        (cb: SBotPolling[IO]) => {
          given Api[IO] = cb.sBotSetup.api
          LogTelegramChat.sendText[IO](
            msg = "Start Polling Bot Successful ✅",
            sBotInfo = cb.sBotSetup.sBotConfig.sBotInfo
          ) >>
            cb.start()
        },
        sBotInfo
      )
      .as(ExitCode.Success)
  }

}
