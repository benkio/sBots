package com.benkio.youtuboanchei0bot

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.model.reply.pho
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.SBotId
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
import com.benkio.youtuboanchei0bot.data.Audio
import com.benkio.youtuboanchei0bot.data.Gif
import com.benkio.youtuboanchei0bot.data.Mix
import com.benkio.youtuboanchei0bot.data.Photo
import com.benkio.youtuboanchei0bot.data.Sticker
import com.benkio.youtuboanchei0bot.data.Video
import fs2.io.net.Network
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.ember.client.*
import org.http4s.implicits.*
import org.http4s.Uri
import telegramium.bots.high.*
import telegramium.bots.InputPartFile
import telegramium.bots.Message

class YouTuboAncheI0BotPolling[F[_]: Parallel: Async: Api: LogWriter](
    val repositoryInput: Repository[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F]
) extends SBotPolling[F](repositoryInput)
    with YouTuboAncheI0Bot[F] {
  override def repository: Repository[F] =
    repositoryInput
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botId = botId)
  override def filteringMatchesMessages: (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botId)
}

class YouTuboAncheI0BotWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    repositoryInput: Repository[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends SBotWebhook[F](uri, path, webhookCertificate, repositoryInput)
    with YouTuboAncheI0Bot[F] {
  override def repository: Repository[F] =
    repositoryInput
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botId = botId)
  override def filteringMatchesMessages: (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botId)
}

trait YouTuboAncheI0Bot[F[_]: Async: LogWriter] extends SBot[F] {

  override val botName: SBotName                   = YouTuboAncheI0Bot.botName
  override val botId: SBotId                       = YouTuboAncheI0Bot.botId
  override val ignoreMessagePrefix: Option[String] = YouTuboAncheI0Bot.ignoreMessagePrefix
  override val triggerListUri: Uri                 = YouTuboAncheI0Bot.triggerListUri
  override val triggerFilename: String             = YouTuboAncheI0Bot.triggerFilename
  val backgroundJobManager: BackgroundJobManager[F]

  override def messageRepliesDataF: F[List[ReplyBundleMessage[F]]] =
    YouTuboAncheI0Bot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF: F[List[ReplyBundleCommand[F]]] =
    YouTuboAncheI0Bot
      .commandRepliesData[F](
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager
      )
      .pure[F]

}
object YouTuboAncheI0Bot {

  val ignoreMessagePrefix: Option[String] = Some("!")
  val botName: SBotName                   = SBotName("YouTuboAncheI0Bot")
  val botId: SBotId                       = SBotId("ytai")
  val triggerListUri: Uri                 =
    uri"https://github.com/benkio/sBots/blob/main/modules/bots/youTuboAncheI0Bot/ytai_triggers.txt"
  val triggerFilename: String = "ytai_triggers.txt"
  val tokenFilename: String   = "ytai_YouTuboAncheI0Bot.token"
  val configNamespace: String = "ytai"

  def messageRepliesAudioData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] =
    Audio.messageRepliesAudioData[F]

  def messageRepliesGifData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] =
    Gif.messageRepliesGifData[F]

  def messageRepliesMixData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] =
    Mix.messageRepliesMixData[F]

  def messageRepliesPhotoData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] =
    Photo.messageRepliesPhotoData[F]

  def messageRepliesStickerData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] =
    Sticker.messageRepliesStickerData[F]

  def messageRepliesVideoData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] =
    Video.messageRepliesVideoData[F]

  def messageRepliesImageData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToPhoto[F](
      "😐",
      "attonito"
    )(
      pho"ytai_Attonito.jpg"
    ),
    ReplyBundleMessage.textToPhoto[F](
      "barbiere",
      "schiuma da barba",
      "pelata"
    )(
      pho"ytai_Barbiere.jpg"
    ),
    ReplyBundleMessage.textToPhoto[F](
      "calzone"
    )(
      pho"ytai_Calzone.jpg"
    )
  )

  def messageRepliesData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] =
    (messageRepliesAudioData[F] ++ messageRepliesGifData[F] ++ messageRepliesMixData[F] ++ messageRepliesPhotoData[
      F
    ] ++ messageRepliesStickerData[
      F
    ] ++ messageRepliesVideoData[
      F
    ] ++ messageRepliesImageData[F])
      .sorted(using ReplyBundle.orderingInstance[F])
      .reverse

  def commandRepliesData[
      F[_]: Async
  ](
      backgroundJobManager: BackgroundJobManager[F],
      dbLayer: DBLayer[F]
  )(using
      log: LogWriter[F]
  ): List[ReplyBundleCommand[F]] =
    CommandPatternsGroup.TriggerGroup.group[F](
      triggerFileUri = triggerListUri,
      botId = botId,
      botName = botName,
      ignoreMessagePrefix = YouTuboAncheI0Bot.ignoreMessagePrefix,
      messageRepliesData = messageRepliesData[F],
      dbMedia = dbLayer.dbMedia,
      dbTimeout = dbLayer.dbTimeout
    ) ++
      CommandPatternsGroup.ShowGroup.group[F](
        dbShow = dbLayer.dbShow,
        dbSubscription = dbLayer.dbSubscription,
        backgroundJobManager = backgroundJobManager,
        botId = botId,
        botName = botName
      ) ++
      List(
        RandomDataCommand.randomDataReplyBundleCommand[F](
          botId = botId,
          dbMedia = dbLayer.dbMedia
        )
      )

  def buildPollingBot[F[_]: Parallel: Async: Network](using
      log: LogWriter[F]
  ): Resource[F, YouTuboAncheI0BotPolling[F]] =
    for {
      httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
      botSetup   <- BotSetup(
        httpClient = httpClient,
        tokenFilename = tokenFilename,
        namespace = configNamespace,
        botId = botId
      )
    } yield new YouTuboAncheI0BotPolling[F](
      repositoryInput = botSetup.repository,
      dbLayer = botSetup.dbLayer,
      backgroundJobManager = botSetup.backgroundJobManager
    )(using Parallel[F], Async[F], botSetup.api, log)

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(using log: LogWriter[F]): Resource[F, YouTuboAncheI0BotWebhook[F]] =
    BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      botId = botId,
      webhookBaseUrl = webhookBaseUrl
    ).map { botSetup =>
      new YouTuboAncheI0BotWebhook[F](
        uri = botSetup.webhookUri,
        path = botSetup.webhookPath,
        repositoryInput = botSetup.repository,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager,
        webhookCertificate = webhookCertificate
      )(using Async[F], botSetup.api, log)
    }
}
