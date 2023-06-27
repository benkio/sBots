package com.benkio.youtuboancheiobot

import cats.effect._
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.{LogLevel, LogLevels, LogWriter}

object YoutuboAncheIoBotMainPolling extends IOApp {

  private def internalRun(logLevel: LogLevel): IO[ExitCode] = {
    implicit val log: LogWriter[IO] = consoleLogUpToLevel(logLevel)
    YoutuboAncheIoBot
      .buildPollingBot[IO, Unit]((ab: YoutuboAncheIoBotPolling[IO]) => ab.start())
      .as(ExitCode.Success)
  }

  def run(args: List[String]): IO[ExitCode] =
    internalRun(LogLevels.Info)
}
