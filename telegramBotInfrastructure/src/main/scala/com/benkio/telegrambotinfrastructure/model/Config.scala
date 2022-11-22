package com.benkio.telegrambotinfrastructure.model

import cats._
import cats.implicits._
import log.effect.LogWriter
import pureconfig._
import pureconfig.generic.auto._

final case class Config(
    driver: String,
    dbName: String,
    url: String
)

object Config {

  // Try to load the config from normal application.conf and from main application.conf if fails
  def loadConfig[F[_]](namespace: String)(implicit log: LogWriter[F], monadThrow: MonadThrow[F]): F[Config] =
    loadConfigInternal("main." + namespace)(monadThrow).handleErrorWith(err =>
      log.error(
        s"An error occurred loading the rphjbDB configuration from main. Ignore if run thorugh single bot: ${err.getMessage()}"
      ) >>
        loadConfigInternal[F](namespace)(monadThrow)
    )

  private def loadConfigInternal[F[_]](namespace: String)(implicit monadThrow: MonadThrow[F]): F[Config] =
    ConfigSource.default
      .at(namespace)
      .load[Config]
      .fold(
        err => monadThrow.raiseError[Config](new RuntimeException(err.prettyPrint())),
        value => monadThrow.pure(value)
      )
}
