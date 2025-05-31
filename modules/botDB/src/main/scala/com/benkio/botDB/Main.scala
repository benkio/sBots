package com.benkio.botDB

import cats.effect.*
import com.benkio.botDB.config.Config
import com.benkio.botDB.db.DBMigrator
import com.benkio.botDB.media.MediaUpdater
import com.benkio.botDB.show.ShowUpdater
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
      _       <- Resource.eval(IO(log.info("[Main] Initialize: ResourceAccess")))
      resourceAccess = ResourceAccess.fromResources[IO](args.lastOption)
      _ <- Resource.eval(IO(log.info("[Main] Initialize: DBMigrator")))
      migrator = DBMigrator[IO]
      _ <- Resource.eval(IO(log.info("[Main] Initialize: MediaUpdater")))
      mediaUpdater = MediaUpdater(cfg = cfg, dbLayer = dbLayer, resourceAccess = resourceAccess)
      _             <- Resource.eval(IO(log.info("[Main] Fetch Youtube api key from resources")))
      youtubeApiKey <- BotSetup.token(youtubeTokenFilename, resourceAccess)
      showUpdater = ShowUpdater[IO](
        cfg = cfg,
        dbLayer = dbLayer,
        resourceAccess = resourceAccess,
        youtubeApiKey = youtubeApiKey
      )
      _ <- Resource.eval(IO(log.info("[Main] End Initialization. Migrate DB")))
      _ <- Resource.eval(migrator.migrate(cfg))
      _ <- Resource.eval(IO(log.info("[Main] Populate Media Table")))
      _ <- mediaUpdater.updateMedia
      _ <- Resource.eval(IO(log.info("[Main] Populate Show Table")))
      _ <- showUpdater.updateShow
      _ <- Resource.eval(IO(log.info("[Main] Update DB Successful")))
    } yield ()

    program.use_.as(ExitCode.Success)
  }
}
