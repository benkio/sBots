package com.benkio.chattelegramadapter.webhook

import cats.effect.Async
import cats.effect.Resource
import org.http4s.server.Server
import telegramium.bots.high.WebhookBot

final class TelegramWebhookBot[F[_]] private[chattelegramadapter] (
    private[chattelegramadapter] val underlying: AnyRef
)

object TelegramWebhookBot {
  private[chattelegramadapter] def wrap[F[_]](bot: AnyRef): TelegramWebhookBot[F] =
    new TelegramWebhookBot[F](bot)
}

object TelegramWebhookServer {
  def compose[F[_]: Async](
      bots: List[TelegramWebhookBot[F]],
      port: Int,
      host: String,
      keystorePath: Option[String],
      keystorePassword: Option[String]
  ): Resource[F, Server] =
    WebhookBot.compose[F](
      bots = bots.map(_.underlying.asInstanceOf[WebhookBot[F]]),
      port = port,
      host = host,
      keystorePath = keystorePath,
      keystorePassword = keystorePassword
    )
}

