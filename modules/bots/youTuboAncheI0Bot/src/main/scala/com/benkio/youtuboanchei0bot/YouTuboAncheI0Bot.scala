package com.benkio.youtuboanchei0bot

import cats.*
import cats.effect.*
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.model.reply.pho
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomDataCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatternsGroup
import com.benkio.telegrambotinfrastructure.patterns.PostComputationPatterns
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
    override val sBotSetup: BotSetup[F]
) extends SBotPolling[F](sBotSetup)
    with YouTuboAncheI0Bot[F] {
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, sBotId = sBotConfig.sBotInfo.botId)
  override def filteringMatchesMessages: (ReplyBundleMessage, Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, sBotConfig.sBotInfo.botId)
}

class YouTuboAncheI0BotWebhook[F[_]: Async: Api: LogWriter](
    override val sBotSetup: BotSetup[F],
    webhookCertificate: Option[InputPartFile] = None
) extends SBotWebhook[F](sBotSetup, webhookCertificate)
    with YouTuboAncheI0Bot[F] {
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, sBotId = sBotConfig.sBotInfo.botId)
  override def filteringMatchesMessages: (ReplyBundleMessage, Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, sBotConfig.sBotInfo.botId)
}

trait YouTuboAncheI0Bot[F[_]: Applicative] extends SBot[F] {

  override val messageRepliesData: F[List[ReplyBundleMessage]] =
    Applicative[F].pure(YouTuboAncheI0Bot.messageRepliesData)

  override val commandRepliesData: List[ReplyBundleCommand] =
    YouTuboAncheI0Bot.commandRepliesData

}
object YouTuboAncheI0Bot {

  val tokenFilename: String   = "ytai_YouTuboAncheI0Bot.token"
  val configNamespace: String = "ytai"
  val sBotConfig: SBotConfig  = SBotConfig(
    sBotInfo = SBotInfo(SBotId("ytai"), SBotInfo.SBotName("YouTuboAncheI0Bot")),
    triggerFilename = "ytai_triggers.txt",
    triggerListUri = uri"https://github.com/benkio/sBots/blob/main/modules/bots/youTuboAncheI0Bot/ytai_triggers.txt",
    repliesJsonFilename = "ytai_replies.json"
  )

  def messageRepliesAudioData: List[ReplyBundleMessage] =
    Audio.messageRepliesAudioData

  def messageRepliesGifData: List[ReplyBundleMessage] =
    Gif.messageRepliesGifData

  def messageRepliesMixData: List[ReplyBundleMessage] =
    Mix.messageRepliesMixData

  def messageRepliesPhotoData: List[ReplyBundleMessage] =
    Photo.messageRepliesPhotoData

  def messageRepliesStickerData: List[ReplyBundleMessage] =
    Sticker.messageRepliesStickerData

  def messageRepliesVideoData: List[ReplyBundleMessage] =
    Video.messageRepliesVideoData

  def messageRepliesImageData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage.textToPhoto(
      "😐",
      "attonito"
    )(
      pho"ytai_Attonito.jpg"
    ),
    ReplyBundleMessage.textToPhoto(
      "barbiere",
      "schiuma da barba",
      "pelata"
    )(
      pho"ytai_Barbiere.jpg"
    ),
    ReplyBundleMessage.textToPhoto(
      "calzone"
    )(
      pho"ytai_Calzone.jpg"
    )
  )

  val messageRepliesData: List[ReplyBundleMessage] =
    messageRepliesAudioData ++ messageRepliesGifData ++ messageRepliesMixData ++ messageRepliesPhotoData ++ messageRepliesStickerData ++ messageRepliesVideoData ++ messageRepliesImageData

  val commandRepliesData: List[ReplyBundleCommand] =
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

  def buildPollingBot[F[_]: Parallel: Async: Network](using
      log: LogWriter[F]
  ): Resource[F, YouTuboAncheI0BotPolling[F]] =
    for {
      httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
      botSetup   <- BotSetup(
        httpClient = httpClient,
        tokenFilename = tokenFilename,
        namespace = configNamespace,
        sBotConfig = sBotConfig
      )
    } yield new YouTuboAncheI0BotPolling[F](botSetup)(using Parallel[F], Async[F], botSetup.api, log)

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(using log: LogWriter[F]): Resource[F, YouTuboAncheI0BotWebhook[F]] =
    BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      sBotConfig = sBotConfig,
      webhookBaseUrl = webhookBaseUrl
    ).map(botSetup => new YouTuboAncheI0BotWebhook[F](botSetup, webhookCertificate)(using Async[F], botSetup.api, log))
}
