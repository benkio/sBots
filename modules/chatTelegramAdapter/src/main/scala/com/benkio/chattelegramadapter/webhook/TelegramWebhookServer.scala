package com.benkio.chattelegramadapter.webhook

import cats.effect.Async
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chattelegramadapter.SBotWebhook
import com.benkio.chattelegramadapter.TelegramStartupCheck
import log.effect.LogWriter
import org.http4s.server.Server
import telegramium.bots.high.WebhookBot

object TelegramWebhookServer {
  def compose[F[_]: Async: LogWriter](
      bots: List[SBotWebhook[F]],
      port: Int,
      host: String,
      keystorePath: Option[String],
      keystorePassword: Option[String]
  ): Resource[F, Server] =
    for {
      server <- WebhookBot.compose[F](
        bots = bots,
        port = port,
        host = host,
        keystorePath = keystorePath,
        keystorePassword = keystorePassword
      )
      _ <- Resource.eval(
        bots.traverse { bot =>
          TelegramStartupCheck.run(bot.sBotSetup, "Start Webook Bot Successful ✅")
        }
      )
    } yield server
}
