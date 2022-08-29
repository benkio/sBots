package com.benkio.richardphjbensonbot

import cats._
import cats.syntax.all._
import log.effect.LogWriter
import pureconfig._
import pureconfig.generic.auto._

final case class Config(
    driver: String,
    dbName: String,
    url: String
)

object Config {

  val rphjbDBNamespace = "rphjbDB"

  // Try to load the config from normal application.conf and from main application.conf if fails
  def loadConfig[F[_]: MonadThrow](implicit log: LogWriter[F]): F[Config] =
    loadConfig(rphjbDBNamespace).handleErrorWith(err =>
      log.error(
        s"An error occurred loading the rphjbDB configuration. Ignore if run thorugh main: ${err.getMessage()}"
      ) >>
        loadConfig[F]("main." + rphjbDBNamespace)
    )

  def loadConfig[F[_]: MonadThrow](namespace: String): F[Config] =
    ConfigSource.default
      .at(namespace)
      .load[Config]
      .fold(
        err => MonadThrow[F].raiseError[Config](new RuntimeException(err.prettyPrint())),
        value => MonadThrow[F].pure(value)
      )
}
