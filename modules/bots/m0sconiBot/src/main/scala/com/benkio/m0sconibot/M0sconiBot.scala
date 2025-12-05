package com.benkio.m0sconibot

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.m0sconibot.data.Audio
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.model.SBotName
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
    val repositoryInput: Repository[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F]
) extends SBotPolling[F](repositoryInput)
    with M0sconiBot[F] {
  override def repository: Repository[F] =
    repositoryInput
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botId = botId)
  override def filteringMatchesMessages: (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botId)
}

class M0sconiBotWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    repositoryInput: Repository[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends SBotWebhook[F](uri, path, webhookCertificate, repositoryInput)
    with M0sconiBot[F] {
  override def repository: Repository[F] =
    repository
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botId = botId)
  override def filteringMatchesMessages: (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botId)
}

trait M0sconiBot[F[_]: Async: LogWriter] extends SBot[F] {

  override val botId: SBotId                       = M0sconiBot.botId
  override val botName: SBotName                   = M0sconiBot.botName
  override val ignoreMessagePrefix: Option[String] = M0sconiBot.ignoreMessagePrefix
  override val triggerFilename: String             = M0sconiBot.triggerFilename
  override val triggerListUri: Uri                 = M0sconiBot.triggerListUri
  val backgroundJobManager: BackgroundJobManager[F]

  override val messageRepliesDataF: F[List[ReplyBundleMessage[F]]] =
    M0sconiBot.messageRepliesData[F].pure[F]

  override val commandRepliesDataF: F[List[ReplyBundleCommand[F]]] =
    M0sconiBot
      .commandRepliesData[F](
        dbLayer = dbLayer
      )
      .pure[F]

}
object M0sconiBot {

  val ignoreMessagePrefix: Option[String] = Some("!")
  val triggerFilename: String             = "mos_triggers.txt"
  val botName: SBotName                   = SBotName("M0sconiBot")
  val botId: SBotId                       = SBotId("mos")
  val triggerListUri: Uri     = uri"https://github.com/benkio/sBots/blob/main/modules/bots/m0sconiBot/mos_triggers.txt"
  val tokenFilename: String   = "mos_M0sconiBot.token"
  val configNamespace: String = "mos"

  def messageRepliesAudioData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] =
    Audio.messageRepliesAudioData[F]

  def messageRepliesData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] =
    messageRepliesAudioData[F]

  def commandRepliesData[
      F[_]: Async
  ](
      dbLayer: DBLayer[F]
  )(using
      log: LogWriter[F]
  ): List[ReplyBundleCommand[F]] =
    CommandPatternsGroup.TriggerGroup.group[F](
      triggerFileUri = triggerListUri,
      botId = botId,
      botName = botName,
      ignoreMessagePrefix = M0sconiBot.ignoreMessagePrefix,
      messageRepliesData = messageRepliesData[F],
      dbMedia = dbLayer.dbMedia,
      dbTimeout = dbLayer.dbTimeout
    ) ++
      List(
        RandomDataCommand.randomDataReplyBundleCommand[F](
          botId = botId,
          dbMedia = dbLayer.dbMedia
        )
      )

  def buildPollingBot[F[_]: Parallel: Async: Network, A](using log: LogWriter[F]): Resource[F, M0sconiBotPolling[F]] =
    for {
      httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
      botSetup   <- BotSetup(
        httpClient = httpClient,
        tokenFilename = tokenFilename,
        namespace = configNamespace,
        botId = botId
      )
    } yield new M0sconiBotPolling[F](
      repositoryInput = botSetup.repository,
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
      botId = botId,
      webhookBaseUrl = webhookBaseUrl
    ).map { botSetup =>
      new M0sconiBotWebhook[F](
        uri = botSetup.webhookUri,
        path = botSetup.webhookPath,
        repositoryInput = botSetup.repository,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager,
        webhookCertificate = webhookCertificate
      )(using Async[F], botSetup.api, log)
    }
}
