package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import com.benkio.main.Logger.given

object MainPolling extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    MainSetup[IO]().use { mainSetup =>
      GeneralErrorHandling.dbLogAndDie(
        mainSetup.dbLayer.dbLog,
        BotRegistry.value.runPolling()
      )
    }

}
