package com.benkio.botDB.config

import cats.effect.IO
import cats.syntax.either.*
import doobie.Transactor
import pureconfig.*

final case class Config(
    driver: String,
    dbName: String,
    url: String,
    migrationsLocations: List[String],
    migrationsTable: String,
    jsonLocation: List[JsonLocation],
    showConfig: ShowConfig
) derives ConfigReader

object Config {

  def loadConfig(configPath: Option[String] = None): IO[Config] = {
    val source = configPath.fold(ConfigSource.default)(ConfigSource.file)
    IO.fromEither(
      source
        .at("botDB")
        .load[Config]
        .leftMap(err => new RuntimeException(err.prettyPrint()))
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
    applicationName: String
) derives ConfigReader

case class ShowSourceConfig(
    youtubeSources: List[String],
    botId: String,
    captionLanguage: String,
    outputFilePath: String,
    captionFolderPath: String
) derives ConfigReader

case class JsonLocation(botId: String, value: String) derives ConfigReader
