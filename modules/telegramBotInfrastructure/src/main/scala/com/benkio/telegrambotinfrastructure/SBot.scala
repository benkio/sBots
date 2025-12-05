package com.benkio.telegrambotinfrastructure

import cats.*
import cats.data.OptionT
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.messagefiltering.getContent
import com.benkio.telegrambotinfrastructure.messagefiltering.messageType
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringForward
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringOlder
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.reply.ReplyValue
import com.benkio.telegrambotinfrastructure.model.MessageType
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.model.SBotName
import com.benkio.telegrambotinfrastructure.model.Trigger
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.InstructionsCommand
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.Repository
import com.benkio.telegrambotinfrastructure.repository.ResourcesRepository
import com.benkio.telegrambotinfrastructure.http.telegramreply.TelegramReply
import log.effect.LogWriter
import org.http4s.implicits.*
import org.http4s.Uri
import telegramium.bots.high.*
import telegramium.bots.InputPartFile
import telegramium.bots.Message

abstract class SBotPolling[F[_]: Parallel: Async: Api: LogWriter]()
    extends LongPollBot[F](summon[Api[F]])
    with SBot[F] {
  override def onMessage(msg: Message): F[Unit] = onMessageLogic(msg)
}

abstract class SBotWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None,
) extends WebhookBot[F](summon[Api[F]], uri.renderString, path.renderString, certificate = webhookCertificate)
    with SBot[F] {
  override def onMessage(msg: Message): F[Unit] = onMessageLogic(msg)
}

trait SBot[F[_]: Async: LogWriter] {

  // Configuration values & functions /////////////////////////////////////////////////////
  def repository: Repository[F]           = ResourcesRepository.fromResources[F]()
  val ignoreMessagePrefix: Option[String] = Some("!")
  val disableForward: Boolean             = true
  val botName: SBotName
  given botId: SBotId
  val triggerListUri: Uri
  val triggerFilename: String
  val dbLayer: DBLayer[F]
  def filteringMatchesMessages: (ReplyBundleMessage, Message) => Boolean =
    (_: ReplyBundleMessage, _: Message) => true
  def postComputation: Message => F[Unit] = _ => Async[F].unit

  // Reply to Messages ////////////////////////////////////////////////////////

  val messageRepliesData: List[ReplyBundleMessage] =
    List.empty[ReplyBundleMessage]
  val commandRepliesData: List[ReplyBundleCommand] =
    List.empty[ReplyBundleCommand]

  // Bot logic //////////////////////////////////////////////////////////////////////////////

  def allCommandRepliesData: List[ReplyBundleCommand] =
    commandRepliesData :+ InstructionsCommand.instructionsReplyBundleCommand(botId)

  private[telegrambotinfrastructure] def selectReplyBundle(
      msg: Message
  ): Option[ReplyBundleMessage] =
    if FilteringForward.filter(msg, disableForward) && FilteringOlder.filter(msg)
    then messageRepliesData
    .mapFilter(messageReplyBundle =>
      MessageMatches
        .doesMatch(messageReplyBundle, msg, ignoreMessagePrefix)
    ).sortBy(_._1)(using Trigger.orderingInstance.reverse)
      .headOption
      .map(_._2)
    else None

  private[telegrambotinfrastructure] def selectCommandReplyBundle(
      msg: Message
  ): Option[ReplyBundleCommand] =
    msg.text.flatMap(text =>
        allCommandRepliesData.find(rbc =>
          text.startsWith(s"/${rbc.trigger.command} ")
            || text == s"/${rbc.trigger.command}"
            || text.startsWith(s"/${rbc.trigger.command}@${botName}")
        )
      )

  def messageLogic(
      msg: Message
  )(using api: Api[F]): F[Option[List[Message]]] =
    selectReplyBundle(msg)
      .traverse(replyBundle =>
        LogWriter
          .info(s"Computing message ${msg.text} matching message reply bundle triggers: ${replyBundle.trigger} ") *>
          ComputeReply
            .execute[F](
              replyBundle,
              msg,
              filteringMatchesMessages(replyBundle, msg),
              repository,
              dbLayer
            )
      )

  def commandLogic(
      msg: Message
  )(using api: Api[F]): F[Option[List[Message]]] =
    selectCommandReplyBundle(msg)
      .traverse(commandReply =>
        LogWriter.info(s"$botName: Computing command ${msg.text} matching command reply bundle") *>
          ComputeReply.execute[F](
            commandReply,
            msg,
            true,
            repository,
            dbLayer
          )
      )

  private def fileRequestLogic(
      msg: Message
  )(using api: Api[F]): F[Option[List[Message]]] =
    for result <- msg.getContent.fold(Async[F].pure(List.empty))(content =>
      TelegramReply[ReplyValue].reply(
        reply = MediaFile.fromString(content),
        msg = msg,
        repository = repository,
        dbLayer = dbLayer,
        replyToMessage = true
      )
    )
    yield result.some

  private def botLogic(
      msg: Message
  )(using api: Api[F]): F[Option[List[Message]]] =
    msg.messageType(botId) match {
      case MessageType.Message     => messageLogic(msg)
      case MessageType.Command     => commandLogic(msg)
      case MessageType.FileRequest => fileRequestLogic(msg)
    }

  def onMessageLogic(msg: Message)(using api: Api[F]): F[Unit] = {
    val x: OptionT[F, Unit] = for {
      _ <- OptionT.liftF(LogWriter.trace(s"$botName: A message arrived: $msg"))
      _ <- OptionT.liftF(LogWriter.info(s"$botName: A message arrived with content: ${msg.text}"))
      _ <- OptionT(botLogic(msg))
      _ <- OptionT.liftF(postComputation(msg))
    } yield ()
    x.getOrElseF(LogWriter.debug(s"$botName: Input message produced no result: $msg"))
  }
}
