package com.benkio.main

import cats.effect.Async
import com.benkio.telegrambotinfrastructure.Config as DBConfig
import pureconfig.*

final case class Config(
    webhookBaseUrl: String,
    hostUrl: String,
    port: Int,
    webhookCertificate: Option[String],
    keystorePath: Option[String],
    keystorePassword: Option[String],
    mainDB: DBConfig
) derives ConfigReader

object Config {

  def loadConfig[F[_]: Async]: F[Config] =
    ConfigSource.default
      .at("main")
      .load[Config]
      .fold(
        err => Async[F].raiseError[Config](new RuntimeException(err.prettyPrint())),
        value => Async[F].pure(value)
      )
}
