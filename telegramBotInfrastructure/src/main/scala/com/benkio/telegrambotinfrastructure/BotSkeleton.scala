package com.benkio.telegrambotinfrastructure

import cats._
import cats.data.OptionT
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringForward
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageOps
import com.benkio.telegrambotinfrastructure.model.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import log.effect.LogWriter
import org.http4s.Uri
import org.http4s.implicits._
import telegramium.bots.high._
import telegramium.bots.InputPartFile
import telegramium.bots.Message

abstract class BotSkeletonPolling[F[_]: Parallel: Async](implicit
    api: Api[F],
    log: LogWriter[F],
    action: Action[F]
) extends LongPollBot[F](api)
    with BotSkeleton[F] {
  override def onMessage(msg: Message): F[Unit] = {
    val x: OptionT[F, Unit] = for {
      _ <- OptionT.liftF(log.trace(s"$botName: A message arrived: $msg"))
      _ <- OptionT.liftF(log.info(s"$botName: A message arrived with content: ${msg.text}"))
      _ <- OptionT(botLogic(Async[F], log, action)(msg))
      _ <- OptionT.liftF(postComputation(Sync[F])(msg))
    } yield ()
    x.getOrElseF(log.debug(s"$botName: Input message produced no result: $msg"))
  }
}

abstract class BotSkeletonWebhook[F[_]: Async](
    uri: Uri,
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
)(implicit
    api: Api[F],
    log: LogWriter[F],
    action: Action[F]
) extends WebhookBot[F](api, uri.renderString, path.renderString, certificate = webhookCertificate)
    with BotSkeleton[F] {
  override def onMessage(msg: Message): F[Unit] = {
    val x: OptionT[F, Unit] = for {
      _    <- OptionT.liftF(log.trace(s"$botName: A message arrived: $msg"))
      text <- OptionT.fromOption[F](msg.text)
      _    <- OptionT.liftF(log.info(s"$botName: A message arrived with content: $text"))
      _    <- OptionT(botLogic(Async[F], log, action)(msg))
      _    <- OptionT.liftF(postComputation(Sync[F])(msg))
    } yield ()
    x.getOrElseF(log.debug(s"$botName: Input message produced no result: $msg"))
  }
}

trait BotSkeleton[F[_]] {

  // Configuration values & functions /////////////////////////////////////////////////////
  def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = ResourceAccess.fromResources[F]()
  val ignoreMessagePrefix: Option[String]                        = Some("!")
  val disableForward: Boolean                                    = true
  val botName: String
  val botPrefix: String
  val dbLayer: DBLayer[F]
  def filteringMatchesMessages(implicit applicativeF: Applicative[F]): (ReplyBundleMessage[F], Message) => F[Boolean] =
    (_: ReplyBundleMessage[F], _: Message) => applicativeF.pure(true)
  def postComputation(implicit appF: Applicative[F]): Message => F[Unit] = _ => appF.unit

  // Reply to Messages ////////////////////////////////////////////////////////

  def messageRepliesDataF(implicit applicativeF: Applicative[F], log: LogWriter[F]): F[List[ReplyBundleMessage[F]]] =
    log.debug(s"$botName: Empty message reply data") *> List.empty[ReplyBundleMessage[F]].pure[F]
  def commandRepliesDataF(implicit asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    log.debug(s"$botName: Empty command reply data") *> List.empty[ReplyBundleCommand[F]].pure[F]

  // Bot logic //////////////////////////////////////////////////////////////////////////////

  def messageLogic(implicit
      asyncF: Async[F],
      log: LogWriter[F],
      action: Action[F]
  ): (Message) => F[Option[List[Message]]] =
    (msg: Message) =>
      for {
        messageRepliesData <- messageRepliesDataF
        replies <- messageRepliesData
          .find(MessageMatches.doesMatch(_, msg, ignoreMessagePrefix))
          .filter(_ => FilteringForward.filter(msg, disableForward))
          .traverse(replyBundle =>
            log
              .info(s"Computing message ${msg.text} matching message reply bundle triggers: ${replyBundle.trigger} ") *>
              ReplyBundle
                .computeReplyBundle[F](
                  replyBundle,
                  msg,
                  filteringMatchesMessages(Applicative[F])(replyBundle, msg)
                )
          )
      } yield replies

  def commandLogic(implicit
      asyncF: Async[F],
      log: LogWriter[F],
      action: Action[F]
  ): (Message) => F[Option[List[Message]]] =
    (msg: Message) =>
      for {
        commandRepliesData <- commandRepliesDataF
        commandMatch = for {
          text <- msg.text
          result <- commandRepliesData.find(rbc =>
            text.startsWith(s"/${rbc.trigger.command} ")
              || text == s"/${rbc.trigger.command}"
              || text.startsWith(s"/${rbc.trigger.command}@${botName}")
          )
        } yield result
        commands <- commandMatch
          .traverse(commandReply =>
            log.info(s"$botName: Computing command ${msg.text} matching command reply bundle") *>
              ReplyBundle.computeReplyBundle[F](
                commandReply,
                msg,
                Applicative[F].pure(true)
              )
          )
      } yield commands

  def botLogic(implicit
      asyncF: Async[F],
      log: LogWriter[F],
      action: Action[F]
  ): (Message) => F[Option[List[Message]]] =
    (msg: Message) =>
      for {
        messagesOpt <- if (!MessageOps.isCommand(msg)) messageLogic(asyncF, log, action)(msg) else Async[F].pure(None)
        commandsOpt <- commandLogic(asyncF, log, action)(msg)
      } yield SemigroupK[Option].combineK(messagesOpt, commandsOpt)
}
