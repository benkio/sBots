package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter

object MainPolling extends IOApp {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  def run(args: List[String]): IO[ExitCode] =
    MainSetup[IO]().use { mainSetup =>
      GeneralErrorHandling.dbLogAndRestart(
        mainSetup.dbLayer.dbLog,
        BotRegistry.value.runPolling()
      )
    }

}
