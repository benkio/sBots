package com.benkio.mosconibot

import cats.effect._
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.{LogLevel, LogLevels, LogWriter}

object MosconiBotMainPolling extends IOApp {

  private def internalRun(logLevel: LogLevel): IO[ExitCode] = {
    implicit val log: LogWriter[IO] = consoleLogUpToLevel(logLevel)
    MosconiBot
      .buildPollingBot[IO, Unit]((ab: MosconiBotPolling[IO]) => ab.start())
      .as(ExitCode.Success)
  }

  def run(args: List[String]): IO[ExitCode] =
    internalRun(LogLevels.Info)
}
