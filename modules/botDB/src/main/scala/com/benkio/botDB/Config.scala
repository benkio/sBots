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

  def buildTransactor(config: Config): Transactor[IO] =
    Transactor.fromDriverManager[IO](
      config.driver,
      config.url,
      "",
      "",
      None
    )
}

case class ShowConfig(
    showSources: List[ShowSourceConfig],
    runShowFetching: Boolean,
    runShowCaptionFetching: Boolean,
    dryRun: Boolean,
    applicationName: String
) derives ConfigReader

case class ShowSourceConfig(
    youtubeSources: List[String],
    botName: String,
    captionLanguage: String,
    outputFilePath: String
) derives ConfigReader
