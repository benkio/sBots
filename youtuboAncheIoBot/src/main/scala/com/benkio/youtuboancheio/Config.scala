package com.benkio.youtuboancheiobot

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

  val ytaiDBNamespace = "ytaiDB"

  // Try to load the config from normal application.conf and from main application.conf if fails
  def loadConfig[F[_]: MonadThrow](implicit log: LogWriter[F]): F[Config] =
    loadConfig("main." + ytaiDBNamespace).handleErrorWith(err =>
      log.error(
        s"An error occurred loading the ytaiDB configuration from main. Ignore if run thorugh single bot: ${err.getMessage()}"
      ) >>
        loadConfig[F](ytaiDBNamespace)
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
