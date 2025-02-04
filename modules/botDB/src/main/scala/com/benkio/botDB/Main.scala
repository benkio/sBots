package com.benkio.botDB

import com.benkio.botDB.config.Config
import com.benkio.botDB.show.ShowFetcher
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import log.effect.LogLevels
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import cats.effect.*
import com.benkio.botDB.db.BotDBController
import com.benkio.botDB.db.DBMigrator
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import log.effect.LogWriter

object Main extends IOApp {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  // in IT test the args will contain the app config and the stage.
  // Eg ("src/it/resources/app.config", "it")
  def run(args: List[String]): IO[ExitCode] =
    (for {
      _   <- IO(log.info(s"Migrating database configuration"))
      cfg <- Config.loadConfig(args.headOption)
      _   <- IO(log.info(s"Input Configuration: $cfg"))
      transactor = Config.buildTransactor(cfg = cfg)
      dbLayer <- DBLayer[IO](transactor)
      resourceAccess = ResourceAccess.fromResources[IO](args.lastOption)
      migrator       = DBMigrator[IO]
      showFetcher    = ShowFetcher[IO]()
      _ <- IO(log.info(s"Build BotDBController"))
      botDBController = BotDBController[IO](
        cfg = cfg,
        dbLayer = dbLayer,
        resourceAccess = resourceAccess,
        migrator = migrator,
        showFetcher = showFetcher
      )
      _ <- botDBController.build.use_
      _ <- IO(log.info(s"Bot DB Setup Excuted"))
    } yield ()).as(ExitCode.Success)
}
