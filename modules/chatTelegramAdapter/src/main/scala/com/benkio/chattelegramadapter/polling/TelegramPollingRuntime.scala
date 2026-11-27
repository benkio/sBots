package com.benkio.chattelegramadapter.polling

import cats.effect.ExitCode
import cats.effect.IO
import com.benkio.chatcore.llm.LlmDependencies
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chattelegramadapter.SBotMainPolling

object TelegramPollingRuntime {
  def run(
      sBotInfo: SBotInfo,
      opsChatId: Long,
      llmDependencies: Option[LlmDependencies[IO]] = None
  ): IO[ExitCode] =
    SBotMainPolling.run(sBotInfo, opsChatId, llmDependencies)
}
