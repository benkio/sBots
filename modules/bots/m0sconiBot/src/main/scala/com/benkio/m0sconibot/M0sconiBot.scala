package com.benkio.m0sconibot

import cats.*
import cats.effect.*
import com.benkio.m0sconibot.data.Audio
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomDataCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatternsGroup
import com.benkio.telegrambotinfrastructure.patterns.PostComputationPatterns
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.Repository
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.SBot
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

class M0sconiBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    override val repository: Repository[F],
    override val dbLayer: DBLayer[F],
    override val backgroundJobManager: BackgroundJobManager[F]
) extends SBotPolling[F]()
    with M0sconiBot[F] {
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, sBotId = sBotConfig.sBotInfo.botId)
  override def filteringMatchesMessages: (ReplyBundleMessage, Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, sBotConfig.sBotInfo.botId)
}

class M0sconiBotWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    override val repository: Repository[F],
    override val dbLayer: DBLayer[F],
    override val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends SBotWebhook[F](uri, path, webhookCertificate)
    with M0sconiBot[F] {
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, sBotId = sBotConfig.sBotInfo.botId)
  override def filteringMatchesMessages: (ReplyBundleMessage, Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, sBotConfig.sBotInfo.botId)
}

trait M0sconiBot[F[_]] extends SBot[F] {

  override val sBotConfig: SBotConfig = M0sconiBot.sBotConfig
  val backgroundJobManager: BackgroundJobManager[F]

  override val messageRepliesData: List[ReplyBundleMessage] =
    M0sconiBot.messageRepliesData

  override val commandRepliesData: List[ReplyBundleCommand] =
    M0sconiBot.commandRepliesData

}
object M0sconiBot {

  val triggerFilename: String = "mos_triggers.txt"
  val tokenFilename: String   = "mos_M0sconiBot.token"
  val configNamespace: String = "mos"
  val sBotConfig: SBotConfig  = SBotConfig(
    sBotInfo = SBotInfo(SBotId("mos"), SBotInfo.SBotName("M0sconiBot")),
    triggerFilename = triggerFilename,
    triggerListUri = uri"https://github.com/benkio/sBots/blob/main/modules/bots/m0sconiBot/mos_triggers.txt",
    triggerJsonFilename = "mos_replies.json"
  )

  val messageRepliesData: List[ReplyBundleMessage] =
    Audio.messageRepliesAudioData

  val commandRepliesData: List[ReplyBundleCommand] =
    CommandPatternsGroup.TriggerGroup.group(
      triggerFileUri = sBotConfig.triggerListUri,
      sBotInfo = sBotConfig.sBotInfo,
      messageRepliesData = messageRepliesData,
      ignoreMessagePrefix = sBotConfig.ignoreMessagePrefix
    ) ++
      List(
        RandomDataCommand.randomDataReplyBundleCommand(
          sBotInfo = sBotConfig.sBotInfo
        )
      )

  def buildPollingBot[F[_]: Parallel: Async: Network, A](using log: LogWriter[F]): Resource[F, M0sconiBotPolling[F]] =
    for {
      httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
      botSetup   <- BotSetup(
        httpClient = httpClient,
        tokenFilename = tokenFilename,
        namespace = configNamespace,
        sBotConfig = sBotConfig
      )
    } yield new M0sconiBotPolling[F](
      repository = botSetup.repository,
      dbLayer = botSetup.dbLayer,
      backgroundJobManager = botSetup.backgroundJobManager
    )(using Parallel[F], Async[F], botSetup.api, log)

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(using log: LogWriter[F]): Resource[F, M0sconiBotWebhook[F]] =
    BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      sBotConfig = sBotConfig,
      webhookBaseUrl = webhookBaseUrl
    ).map { botSetup =>
      new M0sconiBotWebhook[F](
        uri = botSetup.webhookUri,
        path = botSetup.webhookPath,
        repository = botSetup.repository,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager,
        webhookCertificate = webhookCertificate
      )(using Async[F], botSetup.api, log)
    }
}
