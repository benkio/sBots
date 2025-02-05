package com.benkio.botDB.config

import cats.effect.IO
import doobie.Transactor
import pureconfig.*

final case class Config(
    driver: String,
    dbName: String,
    url: String,
    migrationsLocations: List[String],
    migrationsTable: String,
    jsonLocation: List[String],
    showConfig: ShowConfig
) derives ConfigReader

object Config {

  def loadConfig(configPath: Option[String] = None): IO[Config] = {
    val source = configPath.fold(ConfigSource.default)(ConfigSource.file)
    source
      .at("botDB")
      .load[Config]
      .fold(
        err => IO.raiseError[Config](new RuntimeException(err.prettyPrint())),
        value => IO.pure(value)
      )
  }

  def buildTransactor(cfg: Config): Transactor[IO] =
    Transactor.fromDriverManager[IO](
      cfg.driver,
      cfg.url,
      "",
      "",
      None
    )
}

case class ShowConfig(
    showSources: List[ShowSourceConfig],
    runShowFetching: Boolean,
    dryRun: Boolean
) derives ConfigReader

case class ShowSourceConfig(
    url: List[String],
    botName: String,
    outputFilePath: String
) derives ConfigReader
