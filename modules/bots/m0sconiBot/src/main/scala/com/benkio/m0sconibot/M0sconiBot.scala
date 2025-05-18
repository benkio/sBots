package com.benkio.m0sconibot

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.m0sconibot.data.Audio
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomDataCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatternsGroup
import com.benkio.telegrambotinfrastructure.patterns.PostComputationPatterns
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.BotSkeleton
import com.benkio.telegrambotinfrastructure.BotSkeletonPolling
import com.benkio.telegrambotinfrastructure.BotSkeletonWebhook
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
    val resourceAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F]
) extends BotSkeletonPolling[F](resourceAccess)
    with M0sconiBot[F] {
  override def resourceAccess(using syncF: Async[F], log: LogWriter[F]): ResourceAccess[F] = resourceAccess
  override def postComputation(using appF: Applicative[F]): Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages(using
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

class M0sconiBotWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    resourceAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends BotSkeletonWebhook[F](uri, path, webhookCertificate, resourceAccess)
    with M0sconiBot[F] {
  override def resourceAccess(using syncF: Async[F], log: LogWriter[F]): ResourceAccess[F] = resourceAccess
  override def postComputation(using appF: Applicative[F]): Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages(using
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

trait M0sconiBot[F[_]] extends BotSkeleton[F] {

  override val botName: String                     = M0sconiBot.botName
  override val botPrefix: String                   = M0sconiBot.botPrefix
  override val ignoreMessagePrefix: Option[String] = M0sconiBot.ignoreMessagePrefix
  override val triggerFilename: String             = M0sconiBot.triggerFilename
  override val triggerListUri: Uri                 = M0sconiBot.triggerListUri
  val backgroundJobManager: BackgroundJobManager[F]

  override def messageRepliesDataF(using
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    M0sconiBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(using asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    M0sconiBot
      .commandRepliesData[F](
        dbLayer = dbLayer
      )
      .pure[F]

}
object M0sconiBot {

  val ignoreMessagePrefix: Option[String] = Some("!")
  val triggerFilename: String             = "mos_triggers.txt"
  val botName: String                     = "M0sconiBot"
  val botPrefix: String                   = "mos"
  val triggerListUri: Uri   = uri"https://github.com/benkio/sBots/blob/master/modules/bots/m0sconiBot/mos_triggers.txt"
  val tokenFilename: String = "mos_M0sconiBot.token"
  val configNamespace: String = "mos"

  def messageRepliesAudioData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] =
    Audio.messageRepliesAudioData[F]

  def messageRepliesData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] =
    messageRepliesAudioData[F]
      .sorted(using ReplyBundle.orderingInstance[F])
      .reverse

  def commandRepliesData[
      F[_]: Async
  ](
      dbLayer: DBLayer[F]
  )(using
      log: LogWriter[F]
  ): List[ReplyBundleCommand[F]] =
    CommandPatternsGroup.TriggerGroup.group[F](
      triggerFileUri = triggerListUri,
      botName = botName,
      ignoreMessagePrefix = M0sconiBot.ignoreMessagePrefix,
      messageRepliesData = messageRepliesData[F],
      botPrefix = botPrefix,
      dbMedia = dbLayer.dbMedia,
      dbTimeout = dbLayer.dbTimeout
    ) ++
      List(
        RandomDataCommand.randomDataReplyBundleCommand[F](
          botPrefix = botPrefix,
          dbMedia = dbLayer.dbMedia
        )
      )

  def buildPollingBot[F[_]: Parallel: Async: Network, A](using log: LogWriter[F]): Resource[F, M0sconiBotPolling[F]] =
    for {
      httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
      botSetup <- BotSetup(
        httpClient = httpClient,
        tokenFilename = tokenFilename,
        namespace = configNamespace,
        botName = botName
      )
    } yield new M0sconiBotPolling[F](
      resourceAccess = botSetup.resourceAccess,
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
      botName = botName,
      webhookBaseUrl = webhookBaseUrl
    ).map { botSetup =>
      new M0sconiBotWebhook[F](
        uri = botSetup.webhookUri,
        path = botSetup.webhookPath,
        resourceAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager,
        webhookCertificate = webhookCertificate
      )(using Async[F], botSetup.api, log)
    }
}
