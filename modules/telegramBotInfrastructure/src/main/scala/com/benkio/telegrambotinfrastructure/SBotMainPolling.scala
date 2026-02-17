package com.benkio.telegrambotinfrastructure

import cats.effect.*
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.SBot
import com.benkio.telegrambotinfrastructure.SBotPolling
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevel
import log.effect.LogWriter

object SBotMainPolling {

  def run(logLevel: LogLevel, sBotInfo: SBotInfo): cats.effect.IO[cats.effect.ExitCode] = {
    given log: LogWriter[IO] = consoleLogUpToLevel(logLevel)
    SBot
      .buildPollingBot((cb: SBotPolling[IO]) => cb.start(), sBotInfo)
      .as(ExitCode.Success)
  }

}
