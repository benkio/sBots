package com.benkio.botDB

import cats.effect.IO
import pureconfig._
import pureconfig.generic.auto._

final case class Config(
    driver: String,
    dbName: String,
    user: String,
    password: String,
    host: String,
    port: Int,
    url: String,
    migrationsLocations: List[String],
    migrationsTable: String
)

object Config {

  def loadConfig: IO[Config] =
    ConfigSource.default
      .at("botDB")
      .load[Config]
      .fold(
        err => IO.raiseError[Config](new RuntimeException(err.prettyPrint())),
        value => IO.pure(value)
      )
}