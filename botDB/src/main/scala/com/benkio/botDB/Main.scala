package com.benkio.botDB

import cats.effect._
import com.benkio.botDB.db.BotDBController
import com.benkio.botDB.db.DBMigrator
import com.benkio.botDB.db.DatabaseRepository
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess

object Main extends IOApp {

  // in IT test the args will contain the app config 
  def run(args: List[String]): IO[ExitCode] =
    (for {
      _   <- IO(println(s"Migrating database configuration"))
      cfg <- Config.loadConfig(args.headOption)
      transactor         = Config.buildTransactor(cfg = cfg)
      databaseRepository = DatabaseRepository[IO](transactor)
      resourceAccess     = ResourceAccess.fromResources[IO](args.tail.headOption)
      migrator           = DBMigrator[IO]
      botDBController = BotDBController[IO](
        cfg = cfg,
        databaseRepository = databaseRepository,
        resourceAccess = resourceAccess,
        migrator = migrator
      )
      _ <- botDBController.build.use_
    } yield ()).as(ExitCode.Success)
}
