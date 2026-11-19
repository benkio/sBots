package com.benkio.chattelegramadapter.webhook

import cats.effect.std.Random
import cats.effect.Async
import cats.effect.Resource
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.EffectfulCallback
import com.benkio.chattelegramadapter.initialization.BotSetup
import com.benkio.chattelegramadapter.SBotWebhook
import log.effect.LogWriter
import org.http4s.client.Client
import telegramium.bots.high.Api

import java.nio.file.Path

object TelegramWebhookRuntime {

  def buildWebhookBot[F[_]: Async: Random](
      httpClient: Client[F],
      sBotInfo: SBotInfo,
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[Path] = None,
      commandEffectfulCallback: Map[String, EffectfulCallback[F]] = Map.empty,
      opsChatId: Long
  )(using log: LogWriter[F]): Resource[F, SBotWebhook[F]] =
    BotSetup(
      httpClient = httpClient,
      sBotConfig = SBotConfig.fromSBotInfo(sBotInfo),
      webhookBaseUrl = webhookBaseUrl,
      opsChatId = opsChatId
    ).map { sBotSetup =>
      given Api[F] = sBotSetup.api
      new SBotWebhook[F](
        sBotSetup = sBotSetup,
        webhookCertificate = webhookCertificate,
        commandEffectfulCallback = commandEffectfulCallback
      )
    }
}
