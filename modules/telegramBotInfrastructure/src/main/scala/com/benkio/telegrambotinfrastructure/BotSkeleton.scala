package com.benkio.telegrambotinfrastructure

import cats.*
import cats.data.OptionT
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.messagefiltering.messageType
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringForward
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringOlder
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.MessageType
import com.benkio.telegrambotinfrastructure.model.Trigger
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.InstructionsCommand
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import log.effect.LogWriter
import org.http4s.implicits.*
import org.http4s.Uri
import telegramium.bots.high.*
import telegramium.bots.InputPartFile
import telegramium.bots.Message

abstract class BotSkeletonPolling[F[_]: Parallel: Async: Api: LogWriter](resourceAccess: ResourceAccess[F])
    extends LongPollBot[F](summon[Api[F]])
    with BotSkeleton[F] {
  override def onMessage(msg: Message): F[Unit] = {
    val x: OptionT[F, Unit] = for {
      _ <- OptionT.liftF(LogWriter.trace(s"$botName: A message arrived: $msg"))
      _ <- OptionT.liftF(LogWriter.info(s"$botName: A message arrived with content: ${msg.text}"))
      _ <- OptionT(botLogic(resourceAccess, msg))
      _ <- OptionT.liftF(postComputation(msg))
    } yield ()
    x.getOrElseF(LogWriter.debug(s"$botName: Input message produced no result: $msg"))
  }
}

abstract class BotSkeletonWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None,
    resourceAccess: ResourceAccess[F]
) extends WebhookBot[F](summon[Api[F]], uri.renderString, path.renderString, certificate = webhookCertificate)
    with BotSkeleton[F] {
  override def onMessage(msg: Message): F[Unit] = {
    val x: OptionT[F, Unit] = for {
      _ <- OptionT.liftF(summon[LogWriter[F]].trace(s"$botName: A message arrived: $msg"))
      _ <- OptionT.liftF(summon[LogWriter[F]].info(s"$botName: A message arrived with content: ${msg.text}"))
      _ <- OptionT(botLogic(resourceAccess, msg))
      _ <- OptionT.liftF(postComputation(msg))
    } yield ()
    x.getOrElseF(summon[LogWriter[F]].debug(s"$botName: Input message produced no result: $msg"))
  }
}

trait BotSkeleton[F[_]: Async: LogWriter] {

  // Configuration values & functions /////////////////////////////////////////////////////
  def resourceAccess: ResourceAccess[F]   = ResourceAccess.fromResources[F]()
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
    Async[F].map(messageRepliesDataF)(
      _.mapFilter(messageReplyBundle =>
        MessageMatches
          .doesMatch(messageReplyBundle, msg, ignoreMessagePrefix)
          .filter(_ => FilteringForward.filter(msg, disableForward) && FilteringOlder.filter(msg))
      ).sortBy(_._1)(using Trigger.orderingInstance.reverse)
        .headOption
        .map(_._2)
    )

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
      resourceAccess: ResourceAccess[F],
      msg: Message
  )(using api: Api[F]): F[Option[List[Message]]] =
    Async[F].flatMap(selectReplyBundle(msg))(
      _.traverse(replyBundle =>
        LogWriter
          .info(s"Computing message ${msg.text} matching message reply bundle triggers: ${replyBundle.trigger} ") *>
          ReplyBundle
            .computeReplyBundle[F](
              replyBundle,
              msg,
              filteringMatchesMessages(replyBundle, msg),
              resourceAccess
            )
      )
    )

  def commandLogic(
      resourceAccess: ResourceAccess[F],
      msg: Message
  )(using api: Api[F]): F[Option[List[Message]]] =
    selectCommandReplyBundle(msg).flatMap(
      _.traverse(commandReply =>
        LogWriter.info(s"$botName: Computing command ${msg.text} matching command reply bundle") *>
          ReplyBundle.computeReplyBundle[F](
            commandReply,
            msg,
            Async[F].pure(true),
            resourceAccess
          )
      )
    )

  def botLogic(
      resourceAccess: ResourceAccess[F],
      msg: Message
  )(using api: Api[F]): F[Option[List[Message]]] =
    msg.messageType(botPrefix) match {
      case MessageType.Message     => messageLogic(resourceAccess, msg)
      case MessageType.Command     => commandLogic(resourceAccess, msg)
      case MessageType.FileRequest =>
        LogWriter.info(s"$botName: To be implemented") >> none.pure
    }
}
