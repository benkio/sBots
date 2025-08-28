package com.benkio.telegrambotinfrastructure

import cats.*
import cats.data.OptionT
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.messagefiltering.messageType
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringForward
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringOlder
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.MessageType
import com.benkio.telegrambotinfrastructure.model.Trigger
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.InstructionsCommand
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.Repository
import com.benkio.telegrambotinfrastructure.repository.ResourcesRepository
import log.effect.LogWriter
import org.http4s.implicits.*
import org.http4s.Uri
import telegramium.bots.high.*
import telegramium.bots.InputPartFile
import telegramium.bots.Message

abstract class SBotPolling[F[_]: Parallel: Async: Api: LogWriter](repository: Repository[F])
    extends LongPollBot[F](summon[Api[F]])
    with SBot[F] {
  override def onMessage(msg: Message): F[Unit] = onMessageLogic(repository, msg)
}

abstract class SBotWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None,
    repository: Repository[F]
) extends WebhookBot[F](summon[Api[F]], uri.renderString, path.renderString, certificate = webhookCertificate)
    with SBot[F] {
  override def onMessage(msg: Message): F[Unit] = onMessageLogic(repository, msg)
}

trait SBot[F[_]: Async: LogWriter] {

  // Configuration values & functions /////////////////////////////////////////////////////
  def repository: Repository[F]           = ResourcesRepository.fromResources[F]()
  val ignoreMessagePrefix: Option[String] = Some("!")
  val disableForward: Boolean             = true
  val botName: String
  val botPrefix: String
  val triggerListUri: Uri
  val triggerFilename: String
  val dbLayer: DBLayer[F]
  def filteringMatchesMessages: (ReplyBundleMessage[F], Message) => F[Boolean] =
    (_: ReplyBundleMessage[F], _: Message) => Async[F].pure(true)
  def postComputation: Message => F[Unit] = _ => Async[F].unit

  // Reply to Messages ////////////////////////////////////////////////////////

  def messageRepliesDataF: F[List[ReplyBundleMessage[F]]] =
    Async[F].*>(LogWriter.debug(s"$botName: Empty message reply data"))(
      Async[F].pure(List.empty[ReplyBundleMessage[F]])
    )
  def commandRepliesDataF: F[List[ReplyBundleCommand[F]]] =
    Async[F].*>(LogWriter.debug(s"$botName: Empty command reply data"))(
      Async[F].pure(List.empty[ReplyBundleCommand[F]])
    )

  // Bot logic //////////////////////////////////////////////////////////////////////////////

  def allCommandRepliesDataF: F[List[ReplyBundleCommand[F]]] =
    Async[F].map(commandRepliesDataF)(definedCommands =>
      definedCommands :+ InstructionsCommand.instructionsReplyBundleCommand(
        botName,
        ignoreMessagePrefix,
        definedCommands
      )(using Async[F])
    )

  private[telegrambotinfrastructure] def selectReplyBundle(
      msg: Message
  ): F[Option[ReplyBundleMessage[F]]] =
    if FilteringForward.filter(msg, disableForward) && FilteringOlder.filter(msg)
    then
      Async[F].map(messageRepliesDataF)(
        _.mapFilter(messageReplyBundle =>
          MessageMatches
            .doesMatch(messageReplyBundle, msg, ignoreMessagePrefix)
        ).sortBy(_._1)(using Trigger.orderingInstance.reverse)
          .headOption
          .map(_._2)
      )
    else none.pure

  private[telegrambotinfrastructure] def selectCommandReplyBundle(
      msg: Message
  ): F[Option[ReplyBundleCommand[F]]] =
    for
      allCommands <- allCommandRepliesDataF
      result = msg.text.flatMap(text =>
        allCommands.find(rbc =>
          text.startsWith(s"/${rbc.trigger.command} ")
            || text == s"/${rbc.trigger.command}"
            || text.startsWith(s"/${rbc.trigger.command}@${botName}")
        )
      )
    yield result

  def messageLogic(
      repository: Repository[F],
      msg: Message
  )(using api: Api[F]): F[Option[List[Message]]] =
    Async[F].flatMap(selectReplyBundle(msg))(
      _.traverse(replyBundle =>
        LogWriter
          .info(s"Computing message ${msg.text} matching message reply bundle triggers: ${replyBundle.trigger} ") *>
          ComputeReply
            .execute[F](
              replyBundle,
              msg,
              filteringMatchesMessages(replyBundle, msg),
              repository
            )
      )
    )

  def commandLogic(
      repository: Repository[F],
      msg: Message
  )(using api: Api[F]): F[Option[List[Message]]] =
    selectCommandReplyBundle(msg).flatMap(
      _.traverse(commandReply =>
        LogWriter.info(s"$botName: Computing command ${msg.text} matching command reply bundle") *>
          ComputeReply.execute[F](
            commandReply,
            msg,
            Async[F].pure(true),
            repository
          )
      )
    )

  // TODO: 779 Implement
  private def fileRequestLogic(
      // repository: Repository[F],
      // msg: Message
  ) // (using api: Api[F])
      : F[Option[List[Message]]] = none.pure

  private def botLogic(
      repository: Repository[F],
      msg: Message
  )(using api: Api[F]): F[Option[List[Message]]] =
    msg.messageType(botPrefix) match {
      case MessageType.Message     => messageLogic(repository, msg)
      case MessageType.Command     => commandLogic(repository, msg)
      case MessageType.FileRequest =>
        LogWriter.info(s"$botName: To be implemented") >> fileRequestLogic( // repository, msg
        )
    }

  def onMessageLogic(repository: Repository[F], msg: Message)(using api: Api[F]): F[Unit] = {
    val x: OptionT[F, Unit] = for {
      _ <- OptionT.liftF(LogWriter.trace(s"$botName: A message arrived: $msg"))
      _ <- OptionT.liftF(LogWriter.info(s"$botName: A message arrived with content: ${msg.text}"))
      _ <- OptionT(botLogic(repository, msg))
      _ <- OptionT.liftF(postComputation(msg))
    } yield ()
    x.getOrElseF(LogWriter.debug(s"$botName: Input message produced no result: $msg"))
  }
}
