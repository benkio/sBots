package com.benkio.main

import cats.effect.IO
import pureconfig._

final case class Config(
    webhookBaseUrl: String,
    hostUrl: String,
    port: Int
)

object Config {

  implicit val configReader: ConfigReader[Config] =
    ConfigReader.forProduct3("webhook-base-url", "host-url", "port")(Config.apply)

  def loadConfig: IO[Config] =
    ConfigSource.default
      .at("server")
      .load[Config]
      .fold(
        err => IO.raiseError[Config](new RuntimeException(err.prettyPrint())),
        value => IO.pure(value)
      )
}
