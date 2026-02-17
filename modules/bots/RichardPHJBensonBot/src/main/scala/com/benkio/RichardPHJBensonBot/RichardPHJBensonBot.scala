package com.benkio.RichardPHJBensonBot

import cats.*
import cats.effect.*
import cats.syntax.all.*
import cats.Parallel
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.model.reply.toText
import com.benkio.telegrambotinfrastructure.model.reply.EffectfulKey
import com.benkio.telegrambotinfrastructure.model.reply.EffectfulReply
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.reply.Text
import com.benkio.telegrambotinfrastructure.model.CommandInstructionData
import com.benkio.telegrambotinfrastructure.model.CommandTrigger
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotName
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.*
import com.benkio.telegrambotinfrastructure.patterns.CommandPatternsGroup
import com.benkio.telegrambotinfrastructure.patterns.PostComputationPatterns
import com.benkio.telegrambotinfrastructure.ISBot
import com.benkio.telegrambotinfrastructure.SBotPolling
import com.benkio.telegrambotinfrastructure.SBotWebhook
import fs2.io.net.Network
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.ember.client.*
import org.http4s.implicits.*
import telegramium.bots.high.*
import telegramium.bots.InputPartFile
import telegramium.bots.Message

class RichardPHJBensonBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    override val sBotSetup: BotSetup[F],
    override val messageRepliesData: List[ReplyBundleMessage]
) extends SBotPolling[F](sBotSetup, messageRepliesData, RichardPHJBensonBot.commandRepliesData(messageRepliesData))
    with RichardPHJBensonBot[F] {
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, sBotId = sBotConfig.sBotInfo.botId)
  override def filteringMatchesMessages: (ReplyBundleMessage, Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, sBotConfig.sBotInfo.botId)
}

class RichardPHJBensonBotWebhook[F[_]: Async: Api: LogWriter](
    override val sBotSetup: BotSetup[F],
    override val messageRepliesData: List[ReplyBundleMessage],
    webhookCertificate: Option[InputPartFile] = None
) extends SBotWebhook[F](
      sBotSetup,
      messageRepliesData,
      RichardPHJBensonBot.commandRepliesData(messageRepliesData),
      webhookCertificate
    )
    with RichardPHJBensonBot[F] {
  override def postComputation: Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, sBotId = sBotConfig.sBotInfo.botId)
  override def filteringMatchesMessages: (ReplyBundleMessage, Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, sBotConfig.sBotInfo.botId)
}

trait RichardPHJBensonBot[F[_]: ApplicativeThrow] extends ISBot[F] {

  override val commandEffectfulCallback: Map[String, Message => F[List[Text]]] =
    Map(
      (
        RichardPHJBensonBot.bensonifyKey,
        (msg: Message) =>
          CommandPatterns.handleCommandWithInput[F](
            msg = msg,
            command = RichardPHJBensonBot.bensonifyKey,
            sBotInfo = sBotConfig.sBotInfo,
            computation = t => List(Bensonify.compute(t)).toText.pure[F],
            defaultReply = "E PARLAAAAAAA!!!!",
            ttl = sBotConfig.messageTimeToLive
          )
      )
    )
}

object RichardPHJBensonBot {

  val triggerFilename: String = "rphjb_triggers.txt"
  val tokenFilename: String   = "rphjb_RichardPHJBensonBot.token"
  val configNamespace: String = "rphjb"
  val sBotConfig: SBotConfig  = SBotConfig(
    sBotInfo = SBotInfo(SBotId("rphjb"), SBotName("RichardPHJBensonBot")),
    triggerFilename = triggerFilename,
    triggerListUri = uri"https://github.com/benkio/sBots/blob/main/modules/bots/RichardPHJBensonBot/rphjb_triggers.txt",
    token = tokenFilename,
    listJsonFilename = "rphjb_list.json",
    repliesJsonFilename = "rphjb_replies.json",
    commandsJsonFilename = "rphjb_commands.json"
  )

  val bensonifyKey: String                   = "bensonify"
  val bensonifyCommandDescriptionIta: String =
    s"'/$bensonifyKey 《testo》': Traduce il testo in input nello stesso modo in cui benson lo scriverebbe. Il testo è obbligatorio"
  val bensonifyCommandDescriptionEng: String =
    s"'/$bensonifyKey 《text》': Translate the text in the same way benson would write it. Text input is mandatory"

  def commandRepliesData(messageRepliesData: List[ReplyBundleMessage]): List[ReplyBundleCommand] =
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
        ),
        ReplyBundleCommand(
          trigger = CommandTrigger(bensonifyKey),
          reply = EffectfulReply(
            key = EffectfulKey.Callback(key = bensonifyKey, sBotInfo = sBotConfig.sBotInfo),
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
      httpClient         <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
      botSetup           <- BotSetup(httpClient = httpClient, sBotConfig = sBotConfig)
      messageRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleMessage](RichardPHJBensonBot.sBotConfig.repliesJsonFilename)
      )
    } yield new RichardPHJBensonBotPolling[F](botSetup, messageRepliesData)(using
      Parallel[F],
      Async[F],
      botSetup.api,
      log
    )

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(using log: LogWriter[F]): Resource[F, RichardPHJBensonBotWebhook[F]] = for {
    botSetup           <- BotSetup(httpClient = httpClient, sBotConfig = sBotConfig, webhookBaseUrl = webhookBaseUrl)
    messageRepliesData <- Resource.eval(
      botSetup.jsonDataRepository.loadData[ReplyBundleMessage](RichardPHJBensonBot.sBotConfig.repliesJsonFilename)
    )
  } yield new RichardPHJBensonBotWebhook[F](botSetup, messageRepliesData, webhookCertificate)(using
    Async[F],
    botSetup.api,
    log
  )
}
