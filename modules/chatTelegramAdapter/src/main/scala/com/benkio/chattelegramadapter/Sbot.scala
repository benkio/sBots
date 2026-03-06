package com.benkio.chattelegramadapter

import cats.effect.Async
import cats.effect.Resource
import cats.Parallel
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chattelegramadapter.initialization.BotSetup
import fs2.io.net.Network
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.Uri
import telegramium.bots.high.Api

import java.nio.file.Path

class SBotPolling[F[_]: Parallel: Api](
    override val sBotSetup: BotSetup[F],
    override val messageRepliesData: List[ReplyBundleMessage],
    override val commandRepliesData: List[ReplyBundleCommand],
    override val commandEffectfulCallback: Map[String, Message => F[List[Text]]] = Map.empty
)(using Async[F], LogWriter[F])
    extends ISBotPolling[F](sBotSetup)

class SBotWebhook[F[_]: Api](
    override val sBotSetup: BotSetup[F],
    override val messageRepliesData: List[ReplyBundleMessage],
    override val commandRepliesData: List[ReplyBundleCommand],
    webhookCertificate: Option[Path] = None,
    override val commandEffectfulCallback: Map[String, Message => F[List[Text]]] = Map.empty
)(using Async[F], LogWriter[F])
    extends ISBotWebhook[F](sBotSetup, webhookCertificate)

object SBot {

  def buildSBotConfig(sBotInfo: SBotInfo): SBotConfig =
    SBotConfig(
      sBotInfo = sBotInfo,
      triggerFilename = s"${sBotInfo.botId}_triggers.txt",
      triggerListUri = Uri.unsafeFromString(
        s"https://github.com/benkio/sBots/blob/main/modules/bots/${sBotInfo.botName}/${sBotInfo.botId}_triggers.txt"
      ),
      listJsonFilename = s"${sBotInfo.botId}_list.json",
      showFilename = s"${sBotInfo.botId}_shows.json",
      repliesJsonFilename = s"${sBotInfo.botId}_replies.json",
      commandsJsonFilename = s"${sBotInfo.botId}_commands.json",
      token = s"${sBotInfo.botId}_${sBotInfo.botName}.token"
    )

  def buildPollingBot[F[_]: Parallel: Async: Network, A](
      action: SBotPolling[F] => F[A],
      sBotInfo: SBotInfo,
      commandEffectfulCallback: Map[String, Message => F[List[Text]]] = Map.empty
  )(using log: LogWriter[F]): F[A] = (for {
    httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
    sBotConfig = buildSBotConfig(sBotInfo)
    sBotSetup <- BotSetup(
      httpClient = httpClient,
      sBotConfig = sBotConfig
    )
    messageRepliesData <- Resource.eval(
      sBotSetup.jsonDataRepository.loadData[ReplyBundleMessage](sBotConfig.repliesJsonFilename)
    )
    commandRepliesData <- Resource.eval(
      sBotSetup.jsonDataRepository.loadData[ReplyBundleCommand](sBotConfig.commandsJsonFilename)
    )
  } yield (sBotSetup, messageRepliesData, commandRepliesData)).use {
    case (sBotSetup, messageRepliesData, commandRepliesData) =>
      given Api[F] = sBotSetup.api
      action(
        new SBotPolling[F](
          sBotSetup = sBotSetup,
          messageRepliesData = messageRepliesData,
          commandRepliesData = commandRepliesData,
          commandEffectfulCallback = commandEffectfulCallback
        )
      )
  }

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      sBotInfo: SBotInfo,
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[Path] = None,
      commandEffectfulCallback: Map[String, Message => F[List[Text]]] = Map.empty
  )(using log: LogWriter[F]): Resource[F, SBotWebhook[F]] = {

    val sBotConfig = buildSBotConfig(sBotInfo)
    for {
      sBotSetup <- BotSetup(
        httpClient = httpClient,
        sBotConfig = sBotConfig,
        webhookBaseUrl = webhookBaseUrl
      )
      messageRepliesData <- Resource.eval(
        sBotSetup.jsonDataRepository.loadData[ReplyBundleMessage](sBotConfig.repliesJsonFilename)
      )
      commandRepliesData <- Resource.eval(
        sBotSetup.jsonDataRepository.loadData[ReplyBundleCommand](sBotConfig.commandsJsonFilename)
      )
    } yield {
      given Api[F] = sBotSetup.api
      new SBotWebhook[F](
        sBotSetup = sBotSetup,
        messageRepliesData = messageRepliesData,
        commandRepliesData = commandRepliesData,
        webhookCertificate = webhookCertificate,
        commandEffectfulCallback = commandEffectfulCallback
      )
    }
  }
}
