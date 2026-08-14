package com.benkio.integration

import cats.effect.IO
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter

object Logger {
  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)
}

object WarnLogger {
  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Warn)
}
