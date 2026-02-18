package com.benkio.telegrambotinfrastructure

import cats.effect.Async
import cats.effect.Resource
import cats.Parallel
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.reply.Text
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import fs2.io.net.Network
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.Uri
import telegramium.bots.high.Api
import telegramium.bots.InputPartFile
import telegramium.bots.Message

class SBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    override val sBotSetup: BotSetup[F],
    override val messageRepliesData: List[ReplyBundleMessage],
    override val commandRepliesData: List[ReplyBundleCommand],
    override val commandEffectfulCallback: Map[String, Message => F[List[Text]]] = Map.empty
) extends ISBotPolling[F](sBotSetup)
    with ISBot[F] {}

class SBotWebhook[F[_]: Async: Api: LogWriter](
    override val sBotSetup: BotSetup[F],
    override val messageRepliesData: List[ReplyBundleMessage],
    override val commandRepliesData: List[ReplyBundleCommand],
    webhookCertificate: Option[InputPartFile] = None,
    override val commandEffectfulCallback: Map[String, Message => F[List[Text]]] = Map.empty
) extends ISBotWebhook[F](sBotSetup, webhookCertificate)
    with ISBot[F] {}

object SBot {

  def buildSBotConfig(sBotInfo: SBotInfo): SBotConfig =
    SBotConfig(
      sBotInfo = sBotInfo,
      triggerFilename = s"${sBotInfo.botId}_triggers.txt",
      triggerListUri = Uri.unsafeFromString(
        s"https://github.com/benkio/sBots/blob/main/modules/bots/${sBotInfo.botName}/${sBotInfo.botId}_triggers.txt"
      ),
      listJsonFilename = s"${sBotInfo.botId}_list.json",
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
      action(
        new SBotPolling[F](
          sBotSetup = sBotSetup,
          messageRepliesData = messageRepliesData,
          commandRepliesData = commandRepliesData,
          commandEffectfulCallback = commandEffectfulCallback
        )(using
          Parallel[F],
          Async[F],
          sBotSetup.api,
          log
        )
      )
  }

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      sBotInfo: SBotInfo,
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None,
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
    } yield new SBotWebhook[F](
      sBotSetup = sBotSetup,
      messageRepliesData = messageRepliesData,
      commandRepliesData = commandRepliesData,
      webhookCertificate = webhookCertificate,
      commandEffectfulCallback = commandEffectfulCallback
    )(using
      Async[F],
      sBotSetup.api,
      log
    )
  }
}
