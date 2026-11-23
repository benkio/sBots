package com.benkio.main

import cats.effect.Async
import com.benkio.chatcore.initialization.DBConfig
import com.benkio.chatdiscordadapter.gateway.DiscordGatewayConfig
import com.benkio.chatircadapter.connection.IrcConfig
import com.benkio.chatllm.llm.LlmConfig
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
) derives ConfigReader {

  // The configuration is logged at the startup, and the logs are shipped to Grafana Cloud: no secrets in it
  override def toString: String =
    s"HttpServerConfig($hostUrl,$port,$keystorePath,${keystorePassword.map(_ => Config.redacted)})"
}

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
    irc: IrcConfig,
    mainDB: DBConfig,
    healthcheckPing: HealthcheckPingConfig,
    llm: LlmConfig
) derives ConfigReader

final case class HealthcheckPingConfig(
    endpoint: Uri,
    cron: CronExpr
) derives ConfigReader {

  // Whoever has the ping url can fake the heartbeat of the check: it is a secret
  override def toString: String = s"HealthcheckPingConfig(${Config.redacted},$cron)"
}

object Config {

  private[main] val redacted = "<redacted>"

  def loadConfig[F[_]: Async]: F[Config] =
    ConfigSource.default
      .at("main")
      .load[Config]
      .fold(
        err => Async[F].raiseError[Config](new RuntimeException(err.prettyPrint())),
        value => Async[F].pure(value)
      )
}
