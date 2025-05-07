package com.benkio.telegrambotinfrastructure.initialization

import cats.*
import cats.implicits.*
import log.effect.LogWriter
import pureconfig.*

final case class Config(db: DBConfig) derives ConfigReader
final case class DBConfig(
    driver: String,
    dbName: String,
    url: String
) derives ConfigReader

object Config {

  // Try to load the config from normal application.conf and from main application.conf if fails
  def loadConfig[F[_]: MonadThrow](namespace: String)(using log: LogWriter[F]): F[Config] =
    loadConfigInternal("main." + namespace).handleErrorWith(err =>
      log.error(
        s"An error occurred loading the $namespace configuration from main. Ignore if run thorugh single bot: ${err.getMessage()}"
      ) >>
        loadConfigInternal[F](namespace)
    )

  private def loadConfigInternal[F[_]](namespace: String)(using monadThrow: MonadThrow[F]): F[Config] =
    ConfigSource.default
      .at(namespace)
      .load[Config]
      .fold(
        err => monadThrow.raiseError[Config](new RuntimeException(err.prettyPrint())),
        value => monadThrow.pure(value)
      )
}
