package com.benkio.chattelegramadapter

import cats.*
import cats.data.OptionT
import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.messagefiltering.*
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message as ModelMessage
import com.benkio.chatcore.model.MessageType
import com.benkio.chatcore.model.Trigger
import com.benkio.chatcore.patterns.CommandPatterns.InstructionsCommand
import com.benkio.chatcore.patterns.CommandPatternsGroup
import com.benkio.chatcore.patterns.PostComputationPatterns
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chattelegramadapter.conversions.MessageConversions.*
import com.benkio.chattelegramadapter.http.telegramreply.callbackreply.TelegramCallbackReply
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.MediaFileReply
import com.benkio.chattelegramadapter.initialization.BotSetup
import com.benkio.chattelegramadapter.model.CallbackData
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.CallbackQuery
import telegramium.bots.InputPartFile
import telegramium.bots.Message as TMessage

import java.nio.file.Path

abstract class ISBotPolling[F[_]: Parallel: Api](
    override val sBotSetup: BotSetup[F]
)(using Async[F], LogWriter[F])
    extends LongPollBot[F](summon[Api[F]])
    with ISBot[F] {
  override def onMessage(msg: TMessage): F[Unit]              = onMessageLogic(msg.toModel)
  override def onCallbackQuery(query: CallbackQuery): F[Unit] = onCallbackLogic(query)
}

abstract class ISBotWebhook[F[_]: Api](
    override val sBotSetup: BotSetup[F],
    webhookCertificate: Option[Path] = None
)(using Async[F], LogWriter[F])
    extends WebhookBot[F](
      summon[Api[F]],
      sBotSetup.webhookUri.renderString,
      sBotSetup.webhookPath.renderString,
      certificate = webhookCertificate.map(p => InputPartFile(p.toFile))
    )
    with ISBot[F] {
  override def onMessage(msg: TMessage): F[Unit]              = onMessageLogic(msg.toModel)
  override def onCallbackQuery(query: CallbackQuery): F[Unit] = onCallbackLogic(query)
}

trait ISBot[F[_]: Async: LogWriter] {

  val sBotSetup: BotSetup[F]

  // Configuration values & functions (from BotSetup) ///////////////////////////////////
  def repository: Repository[F]                                                  = sBotSetup.repository
  def sBotConfig: SBotConfig                                                     = sBotSetup.sBotConfig
  def dbLayer: DBLayer[F]                                                        = sBotSetup.dbLayer
  def backgroundJobManager: BackgroundJobManager[F]                              = sBotSetup.backgroundJobManager
  def filteringMatchesMessages: (ReplyBundleMessage, ModelMessage) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, sBotConfig.sBotInfo.botId)
  def postComputation: ModelMessage => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, sBotId = sBotConfig.sBotInfo.botId)

  // Reply to Messages ////////////////////////////////////////////////////////

  val messageRepliesData: List[ReplyBundleMessage] =
    List.empty[ReplyBundleMessage]
  val commandRepliesData: List[ReplyBundleCommand] =
    List.empty[ReplyBundleCommand]

  // Commands //////////////////////////////////////////////////////////////////////////////

  val commandEffectfulCallback: Map[String, ModelMessage => F[List[Text]]] = Map.empty

  lazy val fixedCommands: List[ReplyBundleCommand] = {
    val commandsPattersGroup =
      CommandPatternsGroup.TriggerGroup.group(
        triggerFileUri = sBotConfig.triggerListUri,
        sBotInfo = sBotConfig.sBotInfo,
        messageRepliesData = messageRepliesData,
        ignoreMessagePrefix = sBotConfig.ignoreMessagePrefix
      )
    commandsPattersGroup :+ InstructionsCommand.instructionsReplyBundleCommand(
      sBotInfo = sBotConfig.sBotInfo,
      commands = commandRepliesData ++ commandsPattersGroup,
      ignoreMessagePrefix = sBotConfig.ignoreMessagePrefix
    )
  }

  lazy val allCommandRepliesData: List[ReplyBundleCommand] =
    commandRepliesData ++ fixedCommands

  // Bot logic //////////////////////////////////////////////////////////////////////////////

  private[chattelegramadapter] def selectReplyBundle(
      msg: ModelMessage
  ): Option[ReplyBundleMessage] =
    if !FilteringForward.filter(msg, sBotConfig.disableForward) || !FilteringOlder.filter(msg)
    then None
    else
      messageRepliesData
        .mapFilter(messageReplyBundle =>
          MessageMatches
            .doesMatch(messageReplyBundle, msg, sBotConfig.ignoreMessagePrefix)
        )
        .sortBy(_._1)(using Trigger.orderingInstance.reverse)
        .headOption
        .map(_._2)

  private[chattelegramadapter] def selectCommandReplyBundle(
      msg: ModelMessage
  ): Option[ReplyBundleCommand] =
    msg.text.flatMap(text =>
      allCommandRepliesData.find(rbc =>
        text.startsWith(s"/${rbc.trigger.command} ")
          || text == s"/${rbc.trigger.command}"
          || text.startsWith(s"/${rbc.trigger.command}@${sBotConfig.sBotInfo.botName}")
      )
    )

  def messageLogic(
      msg: ModelMessage
  )(using api: Api[F]): F[Unit] =
    selectReplyBundle(msg)
      .traverse_(replyBundle =>
        for {
          _ <- LogWriter
            .info(s"Computing message ${msg.text} matching message reply bundle triggers: ${replyBundle.trigger} ")
          filter <- filteringMatchesMessages(replyBundle, msg)
          _      <-
            if filter then ComputeReply
              .execute[F](
                replyBundle = replyBundle,
                message = msg,
                repository = repository,
                backgroundJobManager = backgroundJobManager,
                effectfulCallbacks = commandEffectfulCallback,
                dbLayer = dbLayer,
                ttl = sBotConfig.messageTimeToLive
              )
              .void
            else Async[F].unit
        } yield ()
      )

  def commandLogic(
      msg: ModelMessage
  )(using api: Api[F]): F[Unit] =
    selectCommandReplyBundle(msg)
      .traverse_(commandReply =>
        LogWriter.info(
          s"${sBotConfig.sBotInfo.botName}: Computing command ${msg.text} matching command reply bundle"
        ) *>
          ComputeReply
            .execute[F](
              replyBundle = commandReply,
              message = msg,
              repository = repository,
              backgroundJobManager = backgroundJobManager,
              effectfulCallbacks = commandEffectfulCallback,
              dbLayer = dbLayer,
              ttl = sBotConfig.messageTimeToLive
            )
            .void
      )

  private def fileRequestLogic(
      msg: ModelMessage
  )(using api: Api[F]): F[Unit] =
    msg.getContent
      .fold(Async[F].pure(List.empty))(content =>
        MediaFileReply.sendMediaFile(
          reply = MediaFile.fromString(content),
          msg = msg,
          repository = repository,
          replyToMessage = true
        )
      )
      .void

  private def botLogic(
      msg: ModelMessage
  )(using api: Api[F]): F[Unit] =
    msg.messageType(sBotConfig.sBotInfo.botId) match {
      case MessageType.Message     => messageLogic(msg)
      case MessageType.Command     => commandLogic(msg)
      case MessageType.FileRequest => fileRequestLogic(msg)
    }

  def onMessageLogic(msg: ModelMessage)(using api: Api[F]): F[Unit] = for {
    _ <- LogWriter.trace(s"${sBotConfig.sBotInfo.botName}: A message arrived: $msg")
    _ <- LogWriter.info(s"${sBotConfig.sBotInfo.botName}: A message arrived with content: ${msg.text}")
    _ <- botLogic(msg)
    _ <- postComputation(msg)
  } yield ()

  def onCallbackLogic(query: CallbackQuery)(using api: Api[F]): F[Unit] = (for {
    callbackDataString <- OptionT.fromOption(query.data)
    callbackData = CallbackData(callbackDataString)
    msg <- OptionT.fromOption(query.message)
    _   <- OptionT.liftF(Methods.answerCallbackQuery(callbackQueryId = query.id).exec)
    _   <- OptionT.liftF(
      TelegramCallbackReply.reply(
        msg = msg,
        callbackData = callbackData,
        repository = repository,
        sBotConfig = sBotConfig,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = commandEffectfulCallback,
        dbLayer = dbLayer,
        ttl = sBotConfig.messageTimeToLive
      )
    )
  } yield ()).value.void
}
