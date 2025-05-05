package com.benkio.abarberobot

import cats.effect.*
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevel
import log.effect.LogLevels
import log.effect.LogWriter

object ABarberoBotMainPolling extends IOApp {

  private def internalRun(logLevel: LogLevel): IO[ExitCode] = {
    given log: LogWriter[IO] = consoleLogUpToLevel(logLevel)
    ABarberoBot
      .buildPollingBot[IO]
      .use(_.start())
      .as(ExitCode.Success)
  }

  def run(args: List[String]): IO[ExitCode] =
    internalRun(LogLevels.Info)
}
