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
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.InstructionsCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomDataCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.SearchShowCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.StatisticsCommands
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.SubscribeUnsubscribeCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.TimeoutCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.TriggerListCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.TriggerSearchCommand
import com.benkio.telegrambotinfrastructure.patterns.PostComputationPatterns
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.BotSkeleton
import com.benkio.telegrambotinfrastructure.BotSkeletonPolling
import com.benkio.telegrambotinfrastructure.BotSkeletonWebhook
import com.benkio.youtuboanchei0bot.data.Audio
import com.benkio.youtuboanchei0bot.data.Gif
import com.benkio.youtuboanchei0bot.data.Mix
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
    val resourceAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F]
) extends BotSkeletonPolling[F](resourceAccess)
    with YouTuboAncheI0Bot[F] {
  override def resourceAccess(using syncF: Async[F], log: LogWriter[F]): ResourceAccess[F] = resourceAccess
  override def postComputation(using appF: Applicative[F]): Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages(using
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

class YouTuboAncheI0BotWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    resourceAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends BotSkeletonWebhook[F](uri, path, webhookCertificate, resourceAccess)
    with YouTuboAncheI0Bot[F] {
  override def resourceAccess(using syncF: Async[F], log: LogWriter[F]): ResourceAccess[F] = resourceAccess
  override def postComputation(using appF: Applicative[F]): Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages(using
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

trait YouTuboAncheI0Bot[F[_]] extends BotSkeleton[F] {

  override val botName: String                     = YouTuboAncheI0Bot.botName
  override val botPrefix: String                   = YouTuboAncheI0Bot.botPrefix
  override val ignoreMessagePrefix: Option[String] = YouTuboAncheI0Bot.ignoreMessagePrefix
  override val triggerListUri: Uri                 = YouTuboAncheI0Bot.triggerListUri
  override val triggerFilename: String             = YouTuboAncheI0Bot.triggerFilename
  val backgroundJobManager: BackgroundJobManager[F]

  override def messageRepliesDataF(using
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    YouTuboAncheI0Bot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(using asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    YouTuboAncheI0Bot
      .commandRepliesData[F](
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager
      )
      .pure[F]

}
object YouTuboAncheI0Bot {

  val ignoreMessagePrefix: Option[String] = Some("!")
  val botName: String                     = "YouTuboAncheI0Bot"
  val botPrefix: String                   = "ytai"
  val triggerListUri: Uri =
    uri"https://github.com/benkio/sBots/blob/master/modules/bots/youTuboAncheI0Bot/ytai_triggers.txt"
  val triggerFilename: String = "ytai_triggers.txt"
  val tokenFilename: String   = "ytai_YouTuboAncheI0Bot.token"
  val configNamespace: String = "ytaiDB"

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
    )
  )

  def messageRepliesData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] =
    (messageRepliesAudioData[F] ++ messageRepliesGifData[F] ++ messageRepliesMixData[F] ++ messageRepliesStickerData[
      F
    ] ++ messageRepliesVideoData[
      F
    ] ++ messageRepliesImageData[F])
      .sorted(ReplyBundle.orderingInstance[F])
      .reverse

  def commandRepliesData[
      F[_]: Async
  ](
      backgroundJobManager: BackgroundJobManager[F],
      dbLayer: DBLayer[F]
  )(using
      log: LogWriter[F]
  ): List[ReplyBundleCommand[F]] = List(
    TriggerListCommand.triggerListReplyBundleCommand[F](triggerListUri),
    TriggerSearchCommand.triggerSearchReplyBundleCommand[F](
      botName = botName,
      ignoreMessagePrefix = YouTuboAncheI0Bot.ignoreMessagePrefix,
      mdr = messageRepliesData[F]
    ),
    SearchShowCommand.searchShowReplyBundleCommand(
      botName = botName,
      dbShow = dbLayer.dbShow
    ),
    StatisticsCommands.topTwentyReplyBundleCommand[F](
      botPrefix = botPrefix,
      dbMedia = dbLayer.dbMedia
    ),
    SubscribeUnsubscribeCommand.subscribeReplyBundleCommand[F](
      backgroundJobManager = backgroundJobManager,
      botName = botName
    ),
    SubscribeUnsubscribeCommand.unsubscribeReplyBundleCommand[F](
      backgroundJobManager = backgroundJobManager,
      botName = botName
    ),
    SubscribeUnsubscribeCommand.subscriptionsReplyBundleCommand[F](
      dbSubscription = dbLayer.dbSubscription,
      backgroundJobManager = backgroundJobManager,
      botName = botName
    ),
    TimeoutCommand.timeoutReplyBundleCommand[F](
      botName = botName,
      dbTimeout = dbLayer.dbTimeout,
      log = log
    ),
    RandomDataCommand.randomDataReplyBundleCommand[F](
      botPrefix = botPrefix,
      dbMedia = dbLayer.dbMedia
    ),
    InstructionsCommand.instructionsReplyBundleCommand[F](
      botName = botName,
      ignoreMessagePrefix = YouTuboAncheI0Bot.ignoreMessagePrefix,
      commandDescriptionsIta = List(
        TriggerListCommand.triggerListCommandDescriptionIta,
        TriggerSearchCommand.triggerSearchCommandDescriptionIta,
        SearchShowCommand.searchShowCommandIta,
        StatisticsCommands.topTwentyTriggersCommandDescriptionIta,
        SubscribeUnsubscribeCommand.subscribeCommandDescriptionIta,
        SubscribeUnsubscribeCommand.unsubscribeCommandDescriptionIta,
        SubscribeUnsubscribeCommand.subscriptionsCommandDescriptionIta,
        TimeoutCommand.timeoutCommandDescriptionIta,
        RandomDataCommand.randomDataCommandIta
      ),
      commandDescriptionsEng = List(
        TriggerListCommand.triggerListCommandDescriptionEng,
        TriggerSearchCommand.triggerSearchCommandDescriptionEng,
        SearchShowCommand.searchShowCommandEng,
        StatisticsCommands.topTwentyTriggersCommandDescriptionEng,
        SubscribeUnsubscribeCommand.subscribeCommandDescriptionEng,
        SubscribeUnsubscribeCommand.unsubscribeCommandDescriptionEng,
        SubscribeUnsubscribeCommand.subscriptionsCommandDescriptionEng,
        TimeoutCommand.timeoutCommandDescriptionEng,
        RandomDataCommand.randomDataCommandEng
      )
    )
  )

  def buildPollingBot[F[_]: Parallel: Async: Network, A](
      action: YouTuboAncheI0BotPolling[F] => F[A]
  )(using log: LogWriter[F]): F[A] = (for {
    httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
    botSetup <- BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      botName = botName
    )
  } yield botSetup).use { botSetup =>
    action(
      new YouTuboAncheI0BotPolling[F](
        resourceAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager
      )(using Parallel[F], Async[F], botSetup.api, log)
    )
  }

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(using log: LogWriter[F]): Resource[F, YouTuboAncheI0BotWebhook[F]] =
    BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      botName = botName,
      webhookBaseUrl = webhookBaseUrl
    ).map { botSetup =>
      new YouTuboAncheI0BotWebhook[F](
        uri = botSetup.webhookUri,
        path = botSetup.webhookPath,
        resourceAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager,
        webhookCertificate = webhookCertificate
      )(using Async[F], botSetup.api, log)
    }
}
