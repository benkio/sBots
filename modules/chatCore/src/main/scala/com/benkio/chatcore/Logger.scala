package com.benkio.chatcore

import cats.effect.IO
import log.effect.fs2.SyncLogWriter.log4sLog
import log.effect.LogWriter
import org.log4s.getLogger

object Logger {
  given logWriter: LogWriter[IO] = log4sLog[IO](getLogger("sbots"))
}
