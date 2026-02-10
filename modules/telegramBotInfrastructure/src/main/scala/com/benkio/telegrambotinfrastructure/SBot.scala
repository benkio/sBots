package com.benkio.telegrambotinfrastructure

import cats.*
import cats.data.OptionT
import cats.effect.*
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.http.telegramreply.MediaFileReply
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.*
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.reply.Text
import com.benkio.telegrambotinfrastructure.model.MessageType
import com.benkio.telegrambotinfrastructure.model.Trigger
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.InstructionsCommand
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.Repository
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.InputPartFile
import telegramium.bots.Message

abstract class SBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    override val sBotSetup: BotSetup[F]
) extends LongPollBot[F](summon[Api[F]])
    with SBot[F] {
  override def onMessage(msg: Message): F[Unit] = onMessageLogic(msg)
}

abstract class SBotWebhook[F[_]: Async: Api: LogWriter](
    override val sBotSetup: BotSetup[F],
    webhookCertificate: Option[InputPartFile] = None
) extends WebhookBot[F](
      summon[Api[F]],
      sBotSetup.webhookUri.renderString,
      sBotSetup.webhookPath.renderString,
      certificate = webhookCertificate
    )
    with SBot[F] {
  override def onMessage(msg: Message): F[Unit] = onMessageLogic(msg)
}

trait SBot[F[_]: Async: LogWriter] {

  val sBotSetup: BotSetup[F]

  // Configuration values & functions (from BotSetup) ///////////////////////////////////
  def repository: Repository[F]                                             = sBotSetup.repository
  def sBotConfig: SBotConfig                                                = sBotSetup.sBotConfig
  def dbLayer: DBLayer[F]                                                   = sBotSetup.dbLayer
  def backgroundJobManager: BackgroundJobManager[F]                         = sBotSetup.backgroundJobManager
  def filteringMatchesMessages: (ReplyBundleMessage, Message) => F[Boolean] =
    (_: ReplyBundleMessage, _: Message) => true.pure[F]
  def postComputation: Message => F[Unit] = _ => Async[F].unit

  // Reply to Messages ////////////////////////////////////////////////////////

  val messageRepliesData: List[ReplyBundleMessage] =
    List.empty[ReplyBundleMessage]
  val commandRepliesData: List[ReplyBundleCommand] =
    List.empty[ReplyBundleCommand]

  // Commands //////////////////////////////////////////////////////////////////////////////

  val commandEffectfulCallback: Map[String, Message => F[List[Text]]] = Map.empty

  lazy val allCommandRepliesData: List[ReplyBundleCommand] = {
    val instructionsCmd = InstructionsCommand.instructionsReplyBundleCommand(
      sBotInfo = sBotConfig.sBotInfo,
      commands = commandRepliesData,
      ignoreMessagePrefix = sBotConfig.ignoreMessagePrefix
    )
    commandRepliesData :+ instructionsCmd
  }

  // Bot logic //////////////////////////////////////////////////////////////////////////////

  private[telegrambotinfrastructure] def selectReplyBundle(
      msg: Message
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

  private[telegrambotinfrastructure] def selectCommandReplyBundle(
      msg: Message
  ): Option[ReplyBundleCommand] =
    msg.text.flatMap(text =>
      allCommandRepliesData.find(rbc =>
        text.startsWith(s"/${rbc.trigger.command} ")
          || text == s"/${rbc.trigger.command}"
          || text.startsWith(s"/${rbc.trigger.command}@${sBotConfig.sBotInfo.botName}")
      )
    )

  def messageLogic(
      msg: Message
  )(using api: Api[F]): F[Option[List[Message]]] =
    selectReplyBundle(msg)
      .traverse(replyBundle =>
        for {
          _ <- LogWriter
            .info(s"Computing message ${msg.text} matching message reply bundle triggers: ${replyBundle.trigger} ")
          filter <- filteringMatchesMessages(replyBundle, msg)
          result <-
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
            else List.empty.pure[F]
        } yield result
      )

  def commandLogic(
      msg: Message
  )(using api: Api[F]): F[Option[List[Message]]] =
    selectCommandReplyBundle(msg)
      .traverse(commandReply =>
        LogWriter.info(
          s"${sBotConfig.sBotInfo.botName}: Computing command ${msg.text} matching command reply bundle"
        ) *>
          ComputeReply.execute[F](
            replyBundle = commandReply,
            message = msg,
            repository = repository,
            backgroundJobManager = backgroundJobManager,
            effectfulCallbacks = commandEffectfulCallback,
            dbLayer = dbLayer,
            ttl = sBotConfig.messageTimeToLive
          )
      )

  private def fileRequestLogic(
      msg: Message
  )(using api: Api[F]): F[Option[List[Message]]] =
    for result <- msg.getContent.fold(Async[F].pure(List.empty))(content =>
      MediaFileReply.sendMediaFile(
        reply = MediaFile.fromString(content),
        msg = msg,
        repository = repository,
        replyToMessage = true
      )
    )
    yield result.some

  private def botLogic(
      msg: Message
  )(using api: Api[F]): F[Option[List[Message]]] =
    msg.messageType(sBotConfig.sBotInfo.botId) match {
      case MessageType.Message     => messageLogic(msg)
      case MessageType.Command     => commandLogic(msg)
      case MessageType.FileRequest => fileRequestLogic(msg)
    }

  def onMessageLogic(msg: Message)(using api: Api[F]): F[Unit] = {
    val x: OptionT[F, Unit] = for {
      _ <- OptionT.liftF(LogWriter.trace(s"${sBotConfig.sBotInfo.botName}: A message arrived: $msg"))
      _ <- OptionT.liftF(LogWriter.info(s"${sBotConfig.sBotInfo.botName}: A message arrived with content: ${msg.text}"))
      _ <- OptionT(botLogic(msg))
      _ <- OptionT.liftF(postComputation(msg))
    } yield ()
    x.getOrElseF(LogWriter.debug(s"${sBotConfig.sBotInfo.botName}: Input message produced no result: $msg"))
  }
}
