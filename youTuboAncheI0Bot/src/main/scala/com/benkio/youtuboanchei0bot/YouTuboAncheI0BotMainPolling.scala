package com.benkio.youtuboanchei0bot

import cats.effect.*
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevel
import log.effect.LogLevels
import log.effect.LogWriter

object YouTuboAncheI0BotMainPolling extends IOApp {

  private def internalRun(logLevel: LogLevel): IO[ExitCode] = {
    given log: LogWriter[IO] = consoleLogUpToLevel(logLevel)
    YouTuboAncheI0Bot
      .buildPollingBot[IO, Unit]((ab: YouTuboAncheI0BotPolling[IO]) => ab.start())
      .as(ExitCode.Success)
  }

  def run(args: List[String]): IO[ExitCode] =
    internalRun(LogLevels.Info)

}
