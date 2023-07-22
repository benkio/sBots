package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import com.benkio.abarberobot.ABarberoBotMainPolling
import com.benkio.calandrobot.CalandroBotMainPolling
import com.benkio.mosconibot.MosconiBotMainPolling
import com.benkio.richardphjbensonbot.RichardPHJBensonBotMainPolling
import com.benkio.xahbot.XahBotMainPolling
import com.benkio.youtuboancheiobot.YoutuboAncheIoBotMainPolling
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter

object MainPolling extends IOApp {

  implicit val log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  def run(args: List[String]): IO[ExitCode] =
    MainSetup[IO]().use { mainSetup =>
      GeneralErrorHandling.dbLogAndRestart(
        mainSetup.dbLayer.dbLog,
        ABarberoBotMainPolling.run(args) &>
          CalandroBotMainPolling.run(args) &>
          RichardPHJBensonBotMainPolling.run(args) &>
          XahBotMainPolling.run(args) &>
          YoutuboAncheIoBotMainPolling.run(args) &>
          MosconiBotMainPolling.run(args)
      )
    }

}
