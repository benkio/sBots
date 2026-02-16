package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import com.benkio.ABarberoBot.ABarberoBotMainPolling
import com.benkio.CalandroBot.CalandroBotMainPolling
import com.benkio.m0sconibot.M0sconiBotMainPolling
import com.benkio.richardphjbensonbot.RichardPHJBensonBotMainPolling
import com.benkio.xahleebot.XahLeeBotMainPolling
import com.benkio.youtuboanchei0bot.YouTuboAncheI0BotMainPolling
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter

object MainPolling extends IOApp {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  def run(args: List[String]): IO[ExitCode] =
    MainSetup[IO]().use { mainSetup =>
      GeneralErrorHandling.dbLogAndRestart(
        mainSetup.dbLayer.dbLog,
        ABarberoBotMainPolling.run(args) &>
          CalandroBotMainPolling.run(args) &>
          RichardPHJBensonBotMainPolling.run(args) &>
          XahLeeBotMainPolling.run(args) &>
          YouTuboAncheI0BotMainPolling.run(args) &>
          M0sconiBotMainPolling.run(args)
      )
    }

}
