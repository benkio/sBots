package com.benkio.botDB

import cats.effect.*
import com.benkio.botDB.config.Config
import com.benkio.botDB.db.BotDBController
import com.benkio.botDB.db.DBMigrator
import com.benkio.botDB.show.ShowFetcher
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter

object Main extends IOApp {

  val youtubeTokenFilename = "youtubeApiKey.token"
  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  // in IT test the args will contain the app config and the stage.
  // Eg ("src/it/resources/app.config", "it")
  def run(args: List[String]): IO[ExitCode] = {
    val program = for {
      _   <- Resource.eval(IO(log.info("[Main] Migrating database configuration")))
      cfg <- Resource.eval(Config.loadConfig(args.headOption))
      _   <- Resource.eval(IO(log.info(s"[Main] Input Configuration: $cfg")))
      _   <- Resource.eval(IO(log.info("[Main] Connect to DB")))
      transactor = Config.buildTransactor(cfg = cfg)
      dbLayer <- Resource.eval(DBLayer[IO](transactor))
      _       <- Resource.eval(IO(log.info("[Main] Initialize DBMigrator & ResourceAccess")))
      resourceAccess = ResourceAccess.fromResources[IO](args.lastOption)
      migrator       = DBMigrator[IO]
      _             <- Resource.eval(IO(log.info("[Main] Fetch Youtube api key from resources")))
      youtubeApiKey <- BotSetup.token(youtubeTokenFilename, resourceAccess)
      showFetcher = ShowFetcher[IO](youtubeApiKey)
      _ <- Resource.eval(IO(log.info("[Main] Initialize BotDBController")))
      botDBController = BotDBController[IO](
        cfg = cfg,
        dbLayer = dbLayer,
        resourceAccess = resourceAccess,
        migrator = migrator
        // showFetcher = showFetcher
      )
      _ <- Resource.eval(IO(log.info("[Main] End Initialization. Execution Starts")))
      _ <- botDBController.build
      _ <- Resource.eval(IO(log.info("Bot DB Setup Excuted")))
    } yield ()

    program.use_.as(ExitCode.Success)
  }
}
