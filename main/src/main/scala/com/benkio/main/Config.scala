package com.benkio.main

import cats.effect.Async
import pureconfig._
import pureconfig.generic.auto._
import com.benkio.telegrambotinfrastructure.model.{Config => DBConfig}

final case class Config(
  webhookBaseUrl: String,
  hostUrl: String,
  port: Int,
  webhookCertificate: Option[String],
  mainDB: DBConfig
)

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
