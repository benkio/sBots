package com.benkio.abarberobot

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.abarberobot.data.Audio
import com.benkio.abarberobot.data.Gif
import com.benkio.abarberobot.data.Mix
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.model.reply.vid
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

class ABarberoBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    resourceAccessInput: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F]
) extends BotSkeletonPolling[F](resourceAccessInput)
    with ABarberoBot[F] {
  override def resourceAccess: ResourceAccess[F] =
    resourceAccessInput
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages: (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

class ABarberoBotWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    resourceAccessInput: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends BotSkeletonWebhook[F](uri, path, webhookCertificate, resourceAccessInput)
    with ABarberoBot[F] {
  override def resourceAccess: ResourceAccess[F] =
    resourceAccess
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages: (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

trait ABarberoBot[F[_]: Async: LogWriter] extends BotSkeleton[F] {

  override val botName: String                     = ABarberoBot.botName
  override val botPrefix: String                   = ABarberoBot.botPrefix
  override val triggerListUri: Uri                 = ABarberoBot.triggerListUri
  override val triggerFilename: String             = ABarberoBot.triggerFilename
  override val ignoreMessagePrefix: Option[String] = ABarberoBot.ignoreMessagePrefix
  val backgroundJobManager: BackgroundJobManager[F]

  override def messageRepliesDataF: F[List[ReplyBundleMessage[F]]] =
    ABarberoBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF: F[List[ReplyBundleCommand[F]]] =
    ABarberoBot
      .commandRepliesData[F](
        backgroundJobManager,
        dbLayer
      )
      .pure[F]
}

object ABarberoBot {

  val ignoreMessagePrefix: Option[String] = Some("!")
  val botName: String                     = "ABarberoBot"
  val botPrefix: String                   = "abar"
  val triggerListUri: Uri = uri"https://github.com/benkio/sBots/blob/main/modules/bots/aBarberoBot/abar_triggers.txt"
  val triggerFilename: String = "abar_triggers.txt"
  val tokenFilename: String   = "abar_ABarberoBot.token"
  val configNamespace: String = "abar"

  def messageRepliesAudioData[F[_]: Applicative]: List[ReplyBundleMessage[F]] =
    Audio.messageRepliesAudioData[F]

  def messageRepliesGifData[F[_]: Applicative]: List[ReplyBundleMessage[F]] =
    Gif.messageRepliesGifData[F]

  def messageRepliesVideoData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToVideo[F](
      "parole longobarde",
      "zuffa",
      "spaccare",
      "arraffare",
      "tanfo"
    )(
      vid"abar_ParoleLongobarde.mp4"
    )
  )

  def messageRepliesMixData[F[_]: Applicative]: List[ReplyBundleMessage[F]] =
    Mix.messageRepliesMixData[F]

  def messageRepliesData[F[_]: Applicative]: List[ReplyBundleMessage[F]] =
    (messageRepliesAudioData[F] ++ messageRepliesGifData[F] ++ messageRepliesVideoData[F] ++ messageRepliesMixData[
      F
    ])
      .sorted(using ReplyBundle.orderingInstance[F])
      .reverse

  def commandRepliesData[F[_]: Async](
      backgroundJobManager: BackgroundJobManager[F],
      dbLayer: DBLayer[F]
  )(using
      log: LogWriter[F]
  ): List[ReplyBundleCommand[F]] =
    CommandPatternsGroup.TriggerGroup.group[F](
      triggerFileUri = triggerListUri,
      botName = botName,
      ignoreMessagePrefix = ABarberoBot.ignoreMessagePrefix,
      messageRepliesData = messageRepliesData[F],
      botPrefix = botPrefix,
      dbMedia = dbLayer.dbMedia,
      dbTimeout = dbLayer.dbTimeout
    ) ++
      CommandPatternsGroup.ShowGroup.group[F](
        dbShow = dbLayer.dbShow,
        dbSubscription = dbLayer.dbSubscription,
        backgroundJobManager = backgroundJobManager,
        botName = botName
      ) ++
      List(
        RandomDataCommand.randomDataReplyBundleCommand[F](
          botPrefix = botPrefix,
          dbMedia = dbLayer.dbMedia
        )
      )

  def buildPollingBot[F[_]: Parallel: Async: Network](using log: LogWriter[F]): Resource[F, ABarberoBotPolling[F]] =
    for {
      httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
      botSetup   <- BotSetup(
        httpClient = httpClient,
        tokenFilename = tokenFilename,
        namespace = configNamespace,
        botName = botName
      )
    } yield new ABarberoBotPolling[F](
      resourceAccessInput = botSetup.resourceAccess,
      dbLayer = botSetup.dbLayer,
      backgroundJobManager = botSetup.backgroundJobManager
    )(using Parallel[F], Async[F], botSetup.api, log)

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(using log: LogWriter[F]): Resource[F, ABarberoBotWebhook[F]] =
    BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      botName = botName,
      webhookBaseUrl = webhookBaseUrl
    ).map { botSetup =>
      new ABarberoBotWebhook[F](
        uri = botSetup.webhookUri,
        path = botSetup.webhookPath,
        resourceAccessInput = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager,
        webhookCertificate = webhookCertificate
      )(using Async[F], botSetup.api, log)
    }
}
