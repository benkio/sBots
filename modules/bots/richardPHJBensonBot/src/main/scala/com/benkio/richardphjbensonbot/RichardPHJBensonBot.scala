package com.benkio.richardphjbensonbot

import cats.effect.*
import cats.implicits.*
import cats.Applicative
import cats.Parallel
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.reply.TextReplyM
import com.benkio.telegrambotinfrastructure.model.CommandInstructionSupportedLanguages
import com.benkio.telegrambotinfrastructure.model.CommandTrigger
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.*
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

class RichardPHJBensonBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    resourceAccessInput: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F]
) extends BotSkeletonPolling[F](resourceAccessInput)
    with RichardPHJBensonBot[F] {
  override def resourceAccess: ResourceAccess[F] =
    resourceAccessInput
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages: (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

class RichardPHJBensonBotWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    resourceAccessInput: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends BotSkeletonWebhook[F](uri, path, webhookCertificate, resourceAccessInput)
    with RichardPHJBensonBot[F] {
  override def resourceAccess: ResourceAccess[F] =
    resourceAccess
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages: (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

trait RichardPHJBensonBot[F[_]: Async: LogWriter] extends BotSkeleton[F] {

  override val botName: String                     = RichardPHJBensonBot.botName
  override val botPrefix: String                   = RichardPHJBensonBot.botPrefix
  override val ignoreMessagePrefix: Option[String] = RichardPHJBensonBot.ignoreMessagePrefix
  override val triggerFilename: String             = RichardPHJBensonBot.triggerFilename
  override val triggerListUri: Uri                 = RichardPHJBensonBot.triggerListUri

  val backgroundJobManager: BackgroundJobManager[F]

  override def messageRepliesDataF: F[List[ReplyBundleMessage[F]]] =
    RichardPHJBensonBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF: F[List[ReplyBundleCommand[F]]] =
    RichardPHJBensonBot
      .commandRepliesData[F](
        backgroundJobManager,
        dbLayer
      )
      .pure[F]

}

object RichardPHJBensonBot {

  import com.benkio.richardphjbensonbot.data.Audio.messageRepliesAudioData
  import com.benkio.richardphjbensonbot.data.Gif.messageRepliesGifData
  import com.benkio.richardphjbensonbot.data.Mix.messageRepliesMixData
  import com.benkio.richardphjbensonbot.data.Special.messageRepliesSpecialData
  import com.benkio.richardphjbensonbot.data.Video.messageRepliesVideoData

  val botName: String                     = "RichardPHJBensonBot"
  val botPrefix: String                   = "rphjb"
  val ignoreMessagePrefix: Option[String] = Some("!")
  val triggerFilename: String             = "rphjb_triggers.txt"
  val triggerListUri: Uri =
    uri"https://github.com/benkio/sBots/blob/master/modules/bots/richardPHJBensonBot/rphjb_triggers.txt"
  val tokenFilename: String   = "rphjb_RichardPHJBensonBot.token"
  val configNamespace: String = "rphjb"

  def messageRepliesData[F[_]: Applicative]: List[ReplyBundleMessage[F]] =
    (messageRepliesAudioData[F] ++ messageRepliesGifData[F] ++ messageRepliesVideoData[F] ++ messageRepliesMixData[
      F
    ] ++ messageRepliesSpecialData[F])
      .sorted(using ReplyBundle.orderingInstance[F])
      .reverse

  val bensonifyCommandDescriptionIta: String =
    "'/bensonify 《testo》': Traduce il testo in input nello stesso modo in cui benson lo scriverebbe. Il testo è obbligatorio"
  val bensonifyCommandDescriptionEng: String =
    "'/bensonify 《text》': Translate the text in the same way benson would write it. Text input is mandatory"

  def commandRepliesData[F[_]: Async](
      backgroundJobManager: BackgroundJobManager[F],
      dbLayer: DBLayer[F]
  )(using
      log: LogWriter[F]
  ): List[ReplyBundleCommand[F]] =
    CommandPatternsGroup.TriggerGroup.group[F](
      triggerFileUri = triggerListUri,
      botName = botName,
      ignoreMessagePrefix = RichardPHJBensonBot.ignoreMessagePrefix,
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
        ),
        ReplyBundleCommand(
          trigger = CommandTrigger("bensonify"),
          reply = TextReplyM[F](
            msg =>
              handleCommandWithInput[F](
                msg,
                "bensonify",
                botName,
                t => List(Bensonify.compute(t)).pure[F],
                "E PARLAAAAAAA!!!!"
              ),
            true
          ),
          instruction = CommandInstructionSupportedLanguages.Instructions(
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
      botSetup <- BotSetup(
        httpClient = httpClient,
        tokenFilename = tokenFilename,
        namespace = configNamespace,
        botName = botName
      )
    } yield new RichardPHJBensonBotPolling[F](
      resourceAccessInput = botSetup.resourceAccess,
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
      botName = botName,
      webhookBaseUrl = webhookBaseUrl
    ).map { botSetup =>
      new RichardPHJBensonBotWebhook[F](
        uri = botSetup.webhookUri,
        path = botSetup.webhookPath,
        resourceAccessInput = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager,
        webhookCertificate = webhookCertificate
      )(using Async[F], botSetup.api, log)
    }
}
