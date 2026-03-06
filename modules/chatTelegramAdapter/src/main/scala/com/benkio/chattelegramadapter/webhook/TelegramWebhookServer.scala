package com.benkio.chattelegramadapter.webhook

import cats.effect.Async
import cats.effect.Resource
import com.benkio.chattelegramadapter.SBotWebhook
import org.http4s.server.Server
import telegramium.bots.high.WebhookBot

object TelegramWebhookServer {
  def compose[F[_]: Async](
      bots: List[SBotWebhook[F]],
      port: Int,
      host: String,
      keystorePath: Option[String],
      keystorePassword: Option[String]
  ): Resource[F, Server] =
    WebhookBot.compose[F](
      bots = bots,
      port = port,
      host = host,
      keystorePath = keystorePath,
      keystorePassword = keystorePassword
    )
}
