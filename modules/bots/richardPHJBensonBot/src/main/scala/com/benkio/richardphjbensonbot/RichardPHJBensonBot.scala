package com.benkio.richardphjbensonbot

import com.benkio.telegrambotinfrastructure.model.reply.EffectfulKey
import cats.effect.*
import cats.Parallel
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.reply.EffectfulReply
import com.benkio.telegrambotinfrastructure.model.CommandInstructionData
import com.benkio.telegrambotinfrastructure.model.CommandTrigger
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.*
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

class RichardPHJBensonBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    override val repository: Repository[F],
    override val dbLayer: DBLayer[F],
    override val backgroundJobManager: BackgroundJobManager[F]
) extends SBotPolling[F]()
    with RichardPHJBensonBot[F] {
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, sBotId = sBotInfo.botId)
  override def filteringMatchesMessages: (ReplyBundleMessage, Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, sBotInfo.botId)
}

class RichardPHJBensonBotWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    override val repository: Repository[F],
    override val dbLayer: DBLayer[F],
    override val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends SBotWebhook[F](uri, path, webhookCertificate)
    with RichardPHJBensonBot[F] {
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, sBotId = sBotInfo.botId)
  override def filteringMatchesMessages: (ReplyBundleMessage, Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, sBotInfo.botId)
}

trait RichardPHJBensonBot[F[_]] extends SBot[F] {

  override val sBotInfo: SBotInfo                  = RichardPHJBensonBot.sBotInfo
  override val ignoreMessagePrefix: Option[String] = RichardPHJBensonBot.ignoreMessagePrefix
  override val triggerFilename: String             = RichardPHJBensonBot.triggerFilename
  override val triggerListUri: Uri                 = RichardPHJBensonBot.triggerListUri

  val backgroundJobManager: BackgroundJobManager[F]

  override val messageRepliesData: List[ReplyBundleMessage] =
    RichardPHJBensonBot.messageRepliesData

  override val commandRepliesData: List[ReplyBundleCommand] =
    RichardPHJBensonBot.commandRepliesData

}

object RichardPHJBensonBot {

  import com.benkio.richardphjbensonbot.data.Audio.messageRepliesAudioData
  import com.benkio.richardphjbensonbot.data.Gif.messageRepliesGifData
  import com.benkio.richardphjbensonbot.data.Mix.messageRepliesMixData
  import com.benkio.richardphjbensonbot.data.Special.messageRepliesSpecialData
  import com.benkio.richardphjbensonbot.data.Video.messageRepliesVideoData

  val botName: SBotInfo.SBotName          = SBotInfo.SBotName("RichardPHJBensonBot")
  val botId: SBotId                       = SBotId("rphjb")
  val sBotInfo: SBotInfo                  = SBotInfo(botId, botName)
  val ignoreMessagePrefix: Option[String] = Some("!")
  val triggerFilename: String             = "rphjb_triggers.txt"
  val triggerListUri: Uri                 =
    uri"https://github.com/benkio/sBots/blob/main/modules/bots/richardPHJBensonBot/rphjb_triggers.txt"
  val tokenFilename: String   = "rphjb_RichardPHJBensonBot.token"
  val configNamespace: String = "rphjb"

  val messageRepliesData: List[ReplyBundleMessage] =
    messageRepliesAudioData ++ messageRepliesGifData ++ messageRepliesVideoData ++ messageRepliesMixData ++ messageRepliesSpecialData

  val bensonifyCommandDescriptionIta: String =
    "'/bensonify 《testo》': Traduce il testo in input nello stesso modo in cui benson lo scriverebbe. Il testo è obbligatorio"
  val bensonifyCommandDescriptionEng: String =
    "'/bensonify 《text》': Translate the text in the same way benson would write it. Text input is mandatory"

  val commandRepliesData: List[ReplyBundleCommand] =
    CommandPatternsGroup.TriggerGroup.group(
      triggerFileUri = triggerListUri,
      sBotInfo = RichardPHJBensonBot.sBotInfo,
      messageRepliesData = messageRepliesData,
      ignoreMessagePrefix = RichardPHJBensonBot.ignoreMessagePrefix
    ) ++
      CommandPatternsGroup.ShowGroup.group(
        sBotInfo = RichardPHJBensonBot.sBotInfo
      ) ++
      List(
        RandomDataCommand.randomDataReplyBundleCommand(
          sBotInfo = RichardPHJBensonBot.sBotInfo
        ),
        ReplyBundleCommand(
          trigger = CommandTrigger("bensonify"),
          reply = EffectfulReply(
            key = EffectfulKey.Callback(key = "bensonify", sBotInfo = RichardPHJBensonBot.sBotInfo),
            replyToMessage = true
          ),
          instruction = CommandInstructionData.Instructions(
            ita = bensonifyCommandDescriptionIta,
            eng = bensonifyCommandDescriptionEng
          )
        )
      )

  def buildPollingBot[F[_]: Parallel: Async: Network](using
      log: LogWriter[F]
  ): Resource[F, RichardPHJBensonBotPolling[F]] =
    for {
      httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
      botSetup   <- BotSetup(
        httpClient = httpClient,
        tokenFilename = tokenFilename,
        namespace = configNamespace,
        sBotInfo = sBotInfo
      )
    } yield new RichardPHJBensonBotPolling[F](
      repository = botSetup.repository,
      dbLayer = botSetup.dbLayer,
      backgroundJobManager = botSetup.backgroundJobManager
    )(using Parallel[F], Async[F], botSetup.api, log)

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(using log: LogWriter[F]): Resource[F, RichardPHJBensonBotWebhook[F]] =
    BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      sBotInfo = sBotInfo,
      webhookBaseUrl = webhookBaseUrl
    ).map { botSetup =>
      new RichardPHJBensonBotWebhook[F](
        uri = botSetup.webhookUri,
        path = botSetup.webhookPath,
        repository = botSetup.repository,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager,
        webhookCertificate = webhookCertificate
      )(using Async[F], botSetup.api, log)
    }
}
