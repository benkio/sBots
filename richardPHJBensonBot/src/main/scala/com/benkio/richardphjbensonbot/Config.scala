package com.benkio.richardphjbensonbot

import cats._
import pureconfig._
import pureconfig.generic.auto._

final case class Config(
    driver: String,
    dbName: String,
    user: String,
    password: String,
    host: String,
    port: String,
    url: String
)

object Config {

  def loadConfig[F[_]: MonadThrow]: F[Config] =
    ConfigSource.default
      .at("rphjbDB")
      .load[Config]
      .fold(
        err => MonadThrow[F].raiseError[Config](new RuntimeException(err.prettyPrint())),
        value => MonadThrow[F].pure(value)
      )
}
