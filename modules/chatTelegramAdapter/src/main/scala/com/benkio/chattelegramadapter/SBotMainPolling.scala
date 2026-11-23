package com.benkio.chattelegramadapter

import cats.effect.*
import com.benkio.chatcore.llm.LlmSettings
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Logger.given

object SBotMainPolling {

  def run(
      sBotInfo: SBotInfo,
      opsChatId: Long,
      llmSettingsFactory: Option[LlmSettings.Factory[cats.effect.IO]] = None
  ): cats.effect.IO[cats.effect.ExitCode] = {
    SBot
      .buildPollingBot(
        (cb: SBotPolling[IO]) => {
          TelegramStartupCheck.run(cb.sBotSetup, "Start Polling Bot Successful ✅") >>
            cb.start()
        },
        sBotInfo,
        opsChatId = opsChatId,
        llmSettingsFactory = llmSettingsFactory
      )
      .as(ExitCode.Success)
  }

}
