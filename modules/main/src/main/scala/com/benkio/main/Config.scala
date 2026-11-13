package com.benkio.main

import cats.effect.Async
import com.benkio.chatcore.initialization.DBConfig
import com.benkio.chatdiscordadapter.gateway.DiscordGatewayConfig
import cron4s.expr.CronExpr
import org.http4s.Uri
import pureconfig.*
import pureconfig.module.cron4s.*
import pureconfig.module.http4s.*

/** The HTTP server of the platforms that receive their messages through it, e.g. the Telegram webhook. */
final case class HttpServerConfig(
    hostUrl: String,
    port: Int,
    keystorePath: Option[String],
    keystorePassword: Option[String]
) derives ConfigReader

/** What is Telegram specific. Every platform has a block like this one (`main.<platform>`): see [[Config]].
  *
  * @param opsChatId
  *   the chat that gets the notifications of the operators
  */
final case class TelegramConfig(
    webhookBaseUrl: String,
    webhookCertificate: Option[String],
    opsChatId: Long
) derives ConfigReader

final case class Config(
    http: HttpServerConfig,
    telegram: TelegramConfig,
    discord: DiscordGatewayConfig,
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
