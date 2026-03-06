package com.benkio.main

import cats.effect.Async
import com.benkio.chatcore.initialization.DBConfig
import cron4s.expr.CronExpr
import org.http4s.Uri
import pureconfig.*
import pureconfig.module.cron4s.*
import pureconfig.module.http4s.*

final case class Config(
    webhookBaseUrl: String,
    hostUrl: String,
    port: Int,
    webhookCertificate: Option[String],
    keystorePath: Option[String],
    keystorePassword: Option[String],
    mainDB: DBConfig,
    healthcheckPing: HealthcheckPingConfig
) derives ConfigReader

final case class HealthcheckPingConfig(
    endpoint: Uri,
    cron: CronExpr
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
