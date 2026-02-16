package com.benkio.telegrambotinfrastructure

import org.http4s.Uri


import cats.Parallel
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import fs2.io.net.Network
import log.effect.LogWriter
import org.http4s.ember.client.EmberClientBuilder
import telegramium.bots.InputPartFile
import telegramium.bots.high.Api
import cats.effect.Async
import cats.effect.Resource
import org.http4s.client.Client

class SBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    override val sBotSetup: BotSetup[F],
    override val messageRepliesData: List[ReplyBundleMessage],
    override val commandRepliesData: List[ReplyBundleCommand]
) extends ISBotPolling[F](sBotSetup)
    with ISBot[F] {}

class SBotWebhook[F[_]: Async: Api: LogWriter](
    override val sBotSetup: BotSetup[F],
    override val messageRepliesData: List[ReplyBundleMessage],
    override val commandRepliesData: List[ReplyBundleCommand],
    webhookCertificate: Option[InputPartFile] = None
) extends ISBotWebhook[F](sBotSetup, webhookCertificate)
    with ISBot[F] {}

object SBot {

  def buildSBotConfig(sBotInfo: SBotInfo): SBotConfig =
    SBotConfig(
      sBotInfo = sBotInfo,
      triggerFilename = s"${sBotInfo.botId}_triggers.txt",
      triggerListUri = Uri.unsafeFromString(s"https://github.com/benkio/sBots/blob/main/modules/bots/${sBotInfo.botName}/${sBotInfo.botId}_triggers.txt"),
      repliesJsonFilename = s"${sBotInfo.botId}_replies.json",
      commandsJsonFilename = s"${sBotInfo.botId}_commands.json",
      token = s"${sBotInfo.botId}_${sBotInfo.botName}.token"
    )

  def buildPollingBot[F[_]: Parallel: Async: Network, A](
    action: SBotPolling[F] => F[A],
    sBotInfo: SBotInfo
  )(using log: LogWriter[F]): F[A] = (for {
    httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
    sBotConfig = buildSBotConfig(sBotInfo)
    botSetup   <- BotSetup(
      httpClient = httpClient,
      sBotConfig = sBotConfig
    )
    messageRepliesData <- Resource.eval(
      botSetup.jsonDataRepository.loadData[ReplyBundleMessage](sBotConfig.repliesJsonFilename)
    )
    commandRepliesData <- Resource.eval(
      botSetup.jsonDataRepository.loadData[ReplyBundleCommand](sBotConfig.commandsJsonFilename)
    )
  } yield (botSetup, messageRepliesData, commandRepliesData)).use {
    case (botSetup, messageRepliesData, commandRepliesData) =>
      action(
        new SBotPolling[F](
          sBotSetup = botSetup,
          messageRepliesData = messageRepliesData,
          commandRepliesData = commandRepliesData
        )(using
          Parallel[F],
          Async[F],
          botSetup.api,
          log
        )
      )
  }

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
    sBotInfo: SBotInfo,
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(using log: LogWriter[F]): Resource[F, SBotWebhook[F]] = {

    val sBotConfig = buildSBotConfig(sBotInfo)
    for {
      botSetup <- BotSetup(
        httpClient = httpClient,
        sBotConfig = sBotConfig,
        webhookBaseUrl = webhookBaseUrl
      )
      messageRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleMessage](sBotConfig.repliesJsonFilename)
      )
      commandRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleCommand](sBotConfig.commandsJsonFilename)
      )
    } yield new SBotWebhook[F](botSetup, messageRepliesData, commandRepliesData, webhookCertificate)(using
      Async[F],
      botSetup.api,
      log
    )
  }
}
