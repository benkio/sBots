package com.benkio.telegrambotinfrastructure

import com.benkio.telegrambotinfrastructure.model.Trigger
import cats.*
import cats.data.OptionT
import cats.effect.*
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringForward
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringOlder
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageOps
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import log.effect.LogWriter
import org.http4s.Uri
import org.http4s.implicits.*
import telegramium.bots.high.*
import telegramium.bots.InputPartFile
import telegramium.bots.Message

abstract class BotSkeletonPolling[F[_]: Parallel: Async: Api: LogWriter](resourceAccess: ResourceAccess[F])
    extends LongPollBot[F](summon[Api[F]])
    with BotSkeleton[F] {
  override def onMessage(msg: Message): F[Unit] = {
    val x: OptionT[F, Unit] = for {
      _ <- OptionT.liftF(summon[LogWriter[F]].trace(s"$botName: A message arrived: $msg"))
      _ <- OptionT.liftF(summon[LogWriter[F]].info(s"$botName: A message arrived with content: ${msg.text}"))
      _ <- OptionT(botLogic(resourceAccess, msg)(using Async[F], summon[Api[F]], summon[LogWriter[F]]))
      _ <- OptionT.liftF(postComputation(msg))
    } yield ()
    x.getOrElseF(summon[LogWriter[F]].debug(s"$botName: Input message produced no result: $msg"))
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
      _ <- OptionT(botLogic(resourceAccess, msg)(using Async[F], summon[Api[F]], summon[LogWriter[F]]))
      _ <- OptionT.liftF(postComputation(msg))
    } yield ()
    x.getOrElseF(summon[LogWriter[F]].debug(s"$botName: Input message produced no result: $msg"))
  }
}

trait BotSkeleton[F[_]] {

  // Configuration values & functions /////////////////////////////////////////////////////
  def resourceAccess(using syncF: Sync[F], log: LogWriter[F]): ResourceAccess[F] = ResourceAccess.fromResources[F]()
  val ignoreMessagePrefix: Option[String]                                        = Some("!")
  val disableForward: Boolean                                                    = true
  val botName: String
  val botPrefix: String
  val triggerListUri: Uri
  val triggerFilename: String
  val dbLayer: DBLayer[F]
  def filteringMatchesMessages(using appF: Applicative[F]): (ReplyBundleMessage[F], Message) => F[Boolean] =
    (_: ReplyBundleMessage[F], _: Message) => Applicative[F].pure(true)
  def postComputation(using appF: Applicative[F]): Message => F[Unit] = _ => Applicative[F].unit

  // Reply to Messages ////////////////////////////////////////////////////////

  def messageRepliesDataF(using applicativeF: Applicative[F], log: LogWriter[F]): F[List[ReplyBundleMessage[F]]] =
    log.debug(s"$botName: Empty message reply data") *> List.empty[ReplyBundleMessage[F]].pure[F]
  def commandRepliesDataF(using asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    log.debug(s"$botName: Empty command reply data") *> List.empty[ReplyBundleCommand[F]].pure[F]

  // Bot logic //////////////////////////////////////////////////////////////////////////////

  private[telegrambotinfrastructure] def selectReplyBundle(
      msg: Message
  )(using asyncF: Async[F], api: Api[F], log: LogWriter[F]): F[Option[ReplyBundleMessage[F]]] =
    messageRepliesDataF.map(
      _.mapFilter(messageReplyBundle =>
        MessageMatches
          .doesMatch(messageReplyBundle, msg, ignoreMessagePrefix)
          .filter(_ => FilteringForward.filter(msg, disableForward) && FilteringOlder.filter(msg))
      ).sortBy(_._1)(Trigger.orderingInstance.reverse)
        .headOption
        .map(_._2)
    )

  private[telegrambotinfrastructure] def selectCommandReplyBundle(
      msg: Message
  )(using asyncF: Async[F], api: Api[F], log: LogWriter[F]): F[Option[ReplyBundleCommand[F]]] =
    commandRepliesDataF(using asyncF, log).map(commandRepliesData =>
      for {
        text <- msg.text
        result <- commandRepliesData.find(rbc =>
          text.startsWith(s"/${rbc.trigger.command} ")
            || text == s"/${rbc.trigger.command}"
            || text.startsWith(s"/${rbc.trigger.command}@${botName}")
        )
      } yield result
    )

  def messageLogic(
      resourceAccess: ResourceAccess[F],
      msg: Message
  )(using asyncF: Async[F], api: Api[F], log: LogWriter[F]): F[Option[List[Message]]] =
    selectReplyBundle(msg).flatMap(
      _.traverse(replyBundle =>
        log
          .info(s"Computing message ${msg.text} matching message reply bundle triggers: ${replyBundle.trigger} ") *>
          ReplyBundle
            .computeReplyBundle[F](
              replyBundle,
              msg,
              filteringMatchesMessages(using Applicative[F])(replyBundle, msg),
              resourceAccess
            )
      )
    )

  def commandLogic(
      resourceAccess: ResourceAccess[F],
      msg: Message
  )(using asyncF: Async[F], api: Api[F], log: LogWriter[F]): F[Option[List[Message]]] =
    selectCommandReplyBundle(msg).flatMap(
      _.traverse(commandReply =>
        log.info(s"$botName: Computing command ${msg.text} matching command reply bundle") *>
          ReplyBundle.computeReplyBundle[F](
            commandReply,
            msg,
            Applicative[F].pure(true),
            resourceAccess
          )
      )
    )

  def botLogic(
      resourceAccess: ResourceAccess[F],
      msg: Message
  )(using asyncF: Async[F], api: Api[F], log: LogWriter[F]): F[Option[List[Message]]] =
    for {
      messagesOpt <-
        if (!MessageOps.isCommand(msg)) messageLogic(resourceAccess, msg)(using asyncF, api, log)
        else Async[F].pure[Option[List[Message]]](None)
      commandsOpt <- commandLogic(resourceAccess, msg)
    } yield SemigroupK[Option].combineK(messagesOpt, commandsOpt)
}
