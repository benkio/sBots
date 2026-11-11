package com.benkio.chattelegramadapter.polling

import cats.effect.ExitCode
import cats.effect.IO
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chattelegramadapter.http.TelegramNotifier
import com.benkio.chattelegramadapter.SBotMainPolling

object TelegramPollingRuntime {
  def run(sBotInfo: SBotInfo, opsChatId: Long = TelegramNotifier.defaultOpsChatId): IO[ExitCode] =
    SBotMainPolling.run(sBotInfo, opsChatId)
}
