package com.benkio.chattelegramadapter

import cats.effect.*
import com.benkio.chatcore.llm.LlmWiring
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Logger.given

object SBotMainPolling {

  def run(
      sBotInfo: SBotInfo,
      opsChatId: Long,
      llmWiring: Option[LlmWiring[cats.effect.IO]] = None
  ): cats.effect.IO[cats.effect.ExitCode] = {
    SBot
      .buildPollingBot(
        (cb: SBotPolling[IO]) => {
          TelegramStartupCheck.run(cb.sBotSetup, "Start Polling Bot Successful ✅") >>
            cb.start()
        },
        sBotInfo,
        opsChatId = opsChatId,
        llmWiring = llmWiring
      )
      .as(ExitCode.Success)
  }

}
