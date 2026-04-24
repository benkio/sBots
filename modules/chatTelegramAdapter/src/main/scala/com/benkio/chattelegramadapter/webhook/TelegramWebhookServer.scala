package com.benkio.chattelegramadapter.webhook

import cats.effect.Async
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chattelegramadapter.http.LogTelegramChat
import com.benkio.chattelegramadapter.SBotWebhook
import org.http4s.server.Server
import telegramium.bots.high.Api
import telegramium.bots.high.WebhookBot

object TelegramWebhookServer {
  def compose[F[_]: Async](
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
          given Api[F] = bot.sBotSetup.api
          LogTelegramChat.sendText[F](
            msg = "Start Webook Bot Successful ✅",
            sBotInfo = bot.sBotSetup.sBotConfig.sBotInfo
          )
        }
      )
    } yield server
}
