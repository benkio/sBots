package com.benkio.richardphjbensonbot

import cats.effect.IO
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

  def loadConfig: IO[Config] =
    ConfigSource.default
      .at("rphjbDB")
      .load[Config]
      .fold(
        err => IO.raiseError[Config](new RuntimeException(err.prettyPrint())),
        value => IO.pure(value)
      )
}
