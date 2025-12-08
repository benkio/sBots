package com.benkio.abarberobot

import cats.*
import cats.effect.*
import com.benkio.abarberobot.data.Audio
import com.benkio.abarberobot.data.Gif
import com.benkio.abarberobot.data.Mix
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.model.reply.vid
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotName
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

class ABarberoBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    override val repository: Repository[F],
    override val dbLayer: DBLayer[F],
    override val backgroundJobManager: BackgroundJobManager[F]
) extends SBotPolling[F]()
    with ABarberoBot[F] {
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, sBotId = sBotInfo.botId)
  override def filteringMatchesMessages: (ReplyBundleMessage, Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, sBotInfo.botId)
}

class ABarberoBotWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    override val repository: Repository[F],
    override val dbLayer: DBLayer[F],
    override val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends SBotWebhook[F](uri, path, webhookCertificate)
    with ABarberoBot[F] {
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, sBotId = sBotInfo.botId)
  override def filteringMatchesMessages: (ReplyBundleMessage, Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, sBotInfo.botId)
}

trait ABarberoBot[F[_]] extends SBot[F] {

  override val sBotInfo: SBotInfo                  = ABarberoBot.sBotInfo
  override val triggerListUri: Uri                 = ABarberoBot.triggerListUri
  override val triggerFilename: String             = ABarberoBot.triggerFilename
  override val ignoreMessagePrefix: Option[String] = ABarberoBot.ignoreMessagePrefix
  val backgroundJobManager: BackgroundJobManager[F]

  override val messageRepliesData: List[ReplyBundleMessage] =
    ABarberoBot.messageRepliesData

  override val commandRepliesData: List[ReplyBundleCommand] =
    ABarberoBot.commandRepliesData
}

object ABarberoBot {

  val ignoreMessagePrefix: Option[String] = Some("!")
  val botName: SBotName                   = SBotName("ABarberoBot")
  val botId: SBotId                       = SBotId("abar")
  val sBotInfo: SBotInfo                  = SBotInfo(botId, botName)
  val triggerListUri: Uri = uri"https://github.com/benkio/sBots/blob/main/modules/bots/aBarberoBot/abar_triggers.txt"
  val triggerFilename: String = "abar_triggers.txt"
  val tokenFilename: String   = "abar_ABarberoBot.token"
  val configNamespace: String = "abar"

  val messageRepliesVideoData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage.textToVideo(
      "parole longobarde",
      "zuffa",
      "spaccare",
      "arraffare",
      "tanfo"
    )(
      vid"abar_ParoleLongobarde.mp4"
    )
  )

  val messageRepliesData: List[ReplyBundleMessage] =
    Audio.messageRepliesAudioData ++ Gif.messageRepliesGifData ++ messageRepliesVideoData ++ Mix.messageRepliesMixData

  val commandRepliesData: List[ReplyBundleCommand] =
    CommandPatternsGroup.TriggerGroup.group(
      triggerFileUri = triggerListUri,
      sBotInfo = ABarberoBot.sBotInfo,
      messageRepliesData = messageRepliesData,
      ignoreMessagePrefix = ABarberoBot.ignoreMessagePrefix
    ) ++
      CommandPatternsGroup.ShowGroup.group(
        sBotInfo = ABarberoBot.sBotInfo
      ) ++
      List(
        RandomDataCommand.randomDataReplyBundleCommand(
          sBotInfo = ABarberoBot.sBotInfo
        )
      )

  def buildPollingBot[F[_]: Parallel: Async: Network](using log: LogWriter[F]): Resource[F, ABarberoBotPolling[F]] =
    for {
      httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
      botSetup   <- BotSetup(
        httpClient = httpClient,
        tokenFilename = tokenFilename,
        namespace = configNamespace,
        sBotInfo = sBotInfo
      )
    } yield new ABarberoBotPolling[F](
      repository = botSetup.repository,
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
      sBotInfo = sBotInfo,
      webhookBaseUrl = webhookBaseUrl
    ).map { botSetup =>
      new ABarberoBotWebhook[F](
        uri = botSetup.webhookUri,
        path = botSetup.webhookPath,
        repository = botSetup.repository,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager,
        webhookCertificate = webhookCertificate
      )(using Async[F], botSetup.api, log)
    }
}
