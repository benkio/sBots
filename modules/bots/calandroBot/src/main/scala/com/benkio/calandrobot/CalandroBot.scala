package com.benkio.calandrobot

import cats.*
import cats.effect.*
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotName
import com.benkio.telegrambotinfrastructure.ISBot
import com.benkio.telegrambotinfrastructure.SBotPolling
import com.benkio.telegrambotinfrastructure.SBotWebhook
import fs2.io.net.Network
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.ember.client.*
import org.http4s.implicits.*
import org.http4s.Uri
import telegramium.bots.high.*
import telegramium.bots.InputPartFile

class CalandroBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    override val sBotSetup: BotSetup[F],
    override val messageRepliesData: List[ReplyBundleMessage],
    override val commandRepliesData: List[ReplyBundleCommand]
) extends SBotPolling[F](sBotSetup)
    with ISBot[F] {}

class CalandroBotWebhook[F[_]: Async: Api: LogWriter](
    override val sBotSetup: BotSetup[F],
    override val messageRepliesData: List[ReplyBundleMessage],
    override val commandRepliesData: List[ReplyBundleCommand],
    webhookCertificate: Option[InputPartFile] = None
) extends SBotWebhook[F](sBotSetup, webhookCertificate)
    with ISBot[F] {}

object CalandroBot {

  val tokenFilename: String   = "cala_CalandroBot.token"
  val configNamespace: String = "cala"
  val sBotConfig: SBotConfig  = SBotConfig(
    sBotInfo = SBotInfo(SBotId("cala"), SBotName("CalandroBot")),
    triggerFilename = "cala_triggers.txt",
    triggerListUri = uri"https://github.com/benkio/sBots/blob/main/modules/bots/CalandroBot/cala_triggers.txt",
    repliesJsonFilename = "cala_replies.json",
    commandsJsonFilename = "cala_commands.json"
  )

  def buildPollingBot[F[_]: Parallel: Async: Network, A](
      action: CalandroBotPolling[F] => F[A]
  )(using log: LogWriter[F]): F[A] = (for {
    httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
    botSetup   <- BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      sBotConfig = sBotConfig
    )
    messageRepliesData <- Resource.eval(
      botSetup.jsonDataRepository.loadData[ReplyBundleMessage](CalandroBot.sBotConfig.repliesJsonFilename)
    )
    commandRepliesData <- Resource.eval(
      botSetup.jsonDataRepository.loadData[ReplyBundleCommand](CalandroBot.sBotConfig.commandsJsonFilename)
    )
  } yield (botSetup, messageRepliesData, commandRepliesData)).use {
    case (botSetup, messageRepliesData, commandRepliesData) =>
      action(
        new CalandroBotPolling[F](
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
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(using log: LogWriter[F]): Resource[F, CalandroBotWebhook[F]] = for {
    botSetup <- BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      sBotConfig = sBotConfig,
      webhookBaseUrl = webhookBaseUrl
    )
    messageRepliesData <- Resource.eval(
      botSetup.jsonDataRepository.loadData[ReplyBundleMessage](CalandroBot.sBotConfig.repliesJsonFilename)
    )
    commandRepliesData <- Resource.eval(
      botSetup.jsonDataRepository.loadData[ReplyBundleCommand](CalandroBot.sBotConfig.commandsJsonFilename)
    )
  } yield new CalandroBotWebhook[F](botSetup, messageRepliesData, commandRepliesData, webhookCertificate)(using
    Async[F],
    botSetup.api,
    log
  )
}
