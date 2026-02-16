package com.benkio.abarberobot

import cats.*
import cats.effect.*
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotName
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomDataCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatternsGroup
import com.benkio.telegrambotinfrastructure.patterns.PostComputationPatterns
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
import telegramium.bots.Message

class ABarberoBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    override val sBotSetup: BotSetup[F],
    override val messageRepliesData: List[ReplyBundleMessage]
) extends SBotPolling[F](sBotSetup)
    with ABarberoBot[F] {
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, sBotId = sBotConfig.sBotInfo.botId)
  override def filteringMatchesMessages: (ReplyBundleMessage, Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, sBotConfig.sBotInfo.botId)
}

class ABarberoBotWebhook[F[_]: Async: Api: LogWriter](
    override val sBotSetup: BotSetup[F],
    override val messageRepliesData: List[ReplyBundleMessage],
    webhookCertificate: Option[InputPartFile] = None
) extends SBotWebhook[F](sBotSetup, webhookCertificate)
    with ABarberoBot[F] {
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, sBotId = sBotConfig.sBotInfo.botId)
  override def filteringMatchesMessages: (ReplyBundleMessage, Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, sBotConfig.sBotInfo.botId)
}

trait ABarberoBot[F[_]] extends ISBot[F] {

  override val commandRepliesData: List[ReplyBundleCommand] =
    ABarberoBot.commandRepliesData(messageRepliesData)
}

object ABarberoBot {

  val tokenFilename: String   = "abar_ABarberoBot.token"
  val configNamespace: String = "abar"
  val sBotConfig: SBotConfig  = SBotConfig(
    sBotInfo = SBotInfo(SBotId("abar"), SBotName("ABarberoBot")),
    triggerFilename = "abar_triggers.txt",
    triggerListUri = uri"https://github.com/benkio/sBots/blob/main/modules/bots/aBarberoBot/abar_triggers.txt",
    repliesJsonFilename = "abar_replies.json",
    commandsJsonFilename = "abar_commands.json"
  )

  def commandRepliesData(messageRepliesData: List[ReplyBundleMessage]): List[ReplyBundleCommand] =
    CommandPatternsGroup.TriggerGroup.group(
      triggerFileUri = sBotConfig.triggerListUri,
      sBotInfo = sBotConfig.sBotInfo,
      messageRepliesData = messageRepliesData,
      ignoreMessagePrefix = sBotConfig.ignoreMessagePrefix
    ) ++
      CommandPatternsGroup.ShowGroup.group(
        sBotInfo = sBotConfig.sBotInfo
      ) ++
      List(
        RandomDataCommand.randomDataReplyBundleCommand(
          sBotInfo = sBotConfig.sBotInfo
        )
      )

  def buildPollingBot[F[_]: Parallel: Async: Network](using log: LogWriter[F]): Resource[F, ABarberoBotPolling[F]] =
    for {
      httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
      botSetup   <- BotSetup(
        httpClient = httpClient,
        tokenFilename = tokenFilename,
        namespace = configNamespace,
        sBotConfig = sBotConfig
      )
      messageRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleMessage](ABarberoBot.sBotConfig.repliesJsonFilename)
      )
    } yield new ABarberoBotPolling[F](botSetup, messageRepliesData)(using Parallel[F], Async[F], botSetup.api, log)

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(using log: LogWriter[F]): Resource[F, ABarberoBotWebhook[F]] = for {
    botSetup <- BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      sBotConfig = sBotConfig,
      webhookBaseUrl = webhookBaseUrl
    )
    messageRepliesData <- Resource.eval(
      botSetup.jsonDataRepository.loadData[ReplyBundleMessage](ABarberoBot.sBotConfig.repliesJsonFilename)
    )
  } yield new ABarberoBotWebhook[F](botSetup, messageRepliesData, webhookCertificate)(using Async[F], botSetup.api, log)
}
