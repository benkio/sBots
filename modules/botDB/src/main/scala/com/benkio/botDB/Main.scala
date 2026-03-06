package com.benkio.botDB

import cats.effect.*
import com.benkio.botDB.config.Config
import com.benkio.botDB.db.DBMigrator
import com.benkio.botDB.media.MediaUpdater
import com.benkio.botDB.show.ShowUpdater
import com.benkio.botDB.show.YouTubeService
import com.benkio.botDB.Logger.given
import com.benkio.chatcore.initialization.TokenReader
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.ResourcesRepository
import log.effect.LogWriter

object Main extends IOApp {

  final case class BotDBInitialization(
      config: Config,
      migrator: DBMigrator[IO],
      mediaUpdater: MediaUpdater[IO],
      showUpdater: ShowUpdater[IO]
  )

  val youtubeTokenFilename = "youTubeApiKey.token"

  private[botDB] def initialization(args: List[String]): Resource[IO, BotDBInitialization] =
    for {
      _      <- Resource.eval(LogWriter.info("[Main - Initialization] Migrating database configuration"))
      config <- Resource.eval(Config.loadConfig(args.headOption))
      _      <- Resource.eval(LogWriter.info(s"[Main - Initialization] Input Configuration: $config"))
      _      <- Resource.eval(LogWriter.info("[Main - Initialization] Connect to DB"))
      transactor = Config.buildTransactor(config = config)
      dbLayer <- Resource.eval(DBLayer[IO](transactor))
      _       <- Resource.eval(LogWriter.info("[Main - Initialization] Repository"))
      repository = ResourcesRepository.fromResources[IO](args.lastOption)
      _ <- Resource.eval(LogWriter.info("[Main - Initialization] DBMigrator"))
      migrator = DBMigrator[IO]
      _ <- Resource.eval(LogWriter.info("[Main - Initialization] MediaUpdater"))
      mediaUpdater = MediaUpdater(config = config, dbLayer = dbLayer, repository = repository)
      _              <- Resource.eval(LogWriter.info("[Main - Initialization] Fetch YouTube api key from resources"))
      youTubeApiKey  <- TokenReader.token(youtubeTokenFilename, repository)
      _              <- Resource.eval(LogWriter.info("[Main - Initialization] Creating YouTube Service"))
      youTubeService <- Resource.eval(YouTubeService(config = config, youTubeApiKey))
      _              <- Resource.eval(LogWriter.info("[Main - Initialization] ShowUpdater"))
      showUpdater = ShowUpdater[IO](
        config = config,
        dbLayer = dbLayer,
        youTubeService = youTubeService
      )
    } yield BotDBInitialization(config, migrator, mediaUpdater, showUpdater)

  // in IT test the args will contain the app config and the stage.
  // Eg ("src/it/resources/app.config", "it")
  def run(args: List[String]): IO[ExitCode] = {
    val program = for {
      _                   <- Resource.eval(LogWriter.info("[Main] Start Initialization"))
      botDBInitialization <- initialization(args)
      _                   <- Resource.eval(LogWriter.info("[Main] End Initialization. Migrate DB"))
      _                   <- Resource.eval(botDBInitialization.migrator.migrate(botDBInitialization.config))
      _                   <- Resource.eval(LogWriter.info("[Main] Populate Media Table"))
      _                   <- botDBInitialization.mediaUpdater.updateMedia
      _                   <- Resource.eval(LogWriter.info("[Main] Populate Show Table"))
      _                   <- botDBInitialization.showUpdater.updateShow
      _                   <- Resource.eval(LogWriter.info("[Main] Update DB Successful"))
    } yield ()

    program.use_.as(ExitCode.Success)
  }
}
