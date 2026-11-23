package com.benkio.chattelegramadapter.polling

import cats.effect.ExitCode
import cats.effect.IO
import com.benkio.chatcore.llm.LlmSettings
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chattelegramadapter.SBotMainPolling

object TelegramPollingRuntime {
  def run(
      sBotInfo: SBotInfo,
      opsChatId: Long,
      llmSettingsFactory: Option[LlmSettings.Factory[IO]] = None
  ): IO[ExitCode] =
    SBotMainPolling.run(sBotInfo, opsChatId, llmSettingsFactory)
}
