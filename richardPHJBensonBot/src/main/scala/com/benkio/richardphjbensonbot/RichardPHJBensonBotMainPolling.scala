package com.benkio.richardphjbensonbot

import cats.effect._
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevel
import log.effect.LogLevels
import log.effect.LogWriter

object RichardPHJBensonBotMainPolling extends IOApp {

  private def internalRun(logLevel: LogLevel): cats.effect.IO[cats.effect.ExitCode] = {
    implicit val log: LogWriter[IO] = consoleLogUpToLevel(logLevel)
    RichardPHJBensonBot
      .buildPollingBot((rb: RichardPHJBensonBotPolling[IO]) => rb.start())
      .as(ExitCode.Success)
  }

  def run(args: List[String]): IO[ExitCode] =
    internalRun(LogLevels.Info)
}
