package com.benkio.botDB

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val migrate =
      for {
        _   <- IO(println(s"Migrating database configuration"))
        cfg <- Config.loadConfig
        // _   <- DBMigrations.migrate[IO](cfg)
      } yield println(cfg)
    migrate.as(ExitCode.Success)
  }
}
