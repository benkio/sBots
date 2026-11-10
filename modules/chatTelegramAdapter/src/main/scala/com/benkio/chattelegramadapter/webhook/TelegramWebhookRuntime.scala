package com.benkio.chattelegramadapter.webhook

import cats.effect.std.Random
import cats.effect.Async
import cats.effect.Resource
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chattelegramadapter.initialization.BotSetup
import com.benkio.chattelegramadapter.SBotWebhook
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.Uri
import telegramium.bots.high.Api

import java.nio.file.Path

object TelegramWebhookRuntime {

  def buildSBotConfig(sBotInfo: SBotInfo): SBotConfig =
    SBotConfig(
      sBotInfo = sBotInfo,
      triggerFilename = s"${sBotInfo.botId}_triggers.md",
      triggerListUri = Uri.unsafeFromString(
        s"https://github.com/benkio/sBots/blob/main/modules/bots/${sBotInfo.botName}/${sBotInfo.botId}_triggers.md"
      ),
      listJsonFilename = s"${sBotInfo.botId}_list.json",
      showFilename = s"${sBotInfo.botId}_shows.json",
      repliesJsonFilename = s"${sBotInfo.botId}_replies.json",
      commandsJsonFilename = s"${sBotInfo.botId}_commands.json",
      token = s"${sBotInfo.botId}_${sBotInfo.botName}.token"
    )

  def buildWebhookBot[F[_]: Async: Random](
      httpClient: Client[F],
      sBotInfo: SBotInfo,
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[Path] = None,
      commandEffectfulCallback: Map[String, Message => F[ReplyValue]] = Map.empty
  )(using log: LogWriter[F]): Resource[F, SBotWebhook[F]] =
    BotSetup(
      httpClient = httpClient,
      sBotConfig = buildSBotConfig(sBotInfo),
      webhookBaseUrl = webhookBaseUrl
    ).map { sBotSetup =>
      given Api[F] = sBotSetup.api
      new SBotWebhook[F](
        sBotSetup = sBotSetup,
        webhookCertificate = webhookCertificate,
        commandEffectfulCallback = commandEffectfulCallback
      )
    }
}
