package com.benkio.main

import cats.effect.IO
import pureconfig._
import pureconfig.generic.auto._

final case class Config(
    webhookBaseUrl: String,
    hostUrl: String,
    port: Int
)

object Config {

  def loadConfig: IO[Config] =
    ConfigSource.default
      .at("main")
      .load[Config]
      .fold(
        err => IO.raiseError[Config](new RuntimeException(err.prettyPrint())),
        value => IO.pure(value)
      )
}
