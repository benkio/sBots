package com.benkio.telegrambotinfrastructure

import cats._
import cats.data.OptionT
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccess
import com.benkio.telegrambotinfrastructure.default.DefaultActions
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringForward
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.messagefiltering.Timeout
import com.benkio.telegrambotinfrastructure.model.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.ReplyBundleMessage
import log.effect.LogWriter
import telegramium.bots.Message
import telegramium.bots.high._

import scala.concurrent.duration._

abstract class BotSkeletonPolling[F[_]: Parallel: Async](implicit api: Api[F], log: LogWriter[F])
    extends LongPollBot[F](api)
    with BotSkeleton[F] {
  override def onMessage(msg: Message): F[Unit] = {
    val x: OptionT[F, Unit] = for {
      _ <- OptionT.liftF(log.trace(s"A message arrived: $msg"))
      _ <- OptionT.liftF(log.info(s"A message arrived with content: ${msg.text}"))
      _ <- OptionT(botLogic(Async[F], api, log)(msg))
    } yield ()
    x.getOrElseF(log.debug(s"Input message produced no result: $msg"))
  }
}

abstract class BotSkeletonWebhook[F[_]: Async](url: String, path: String = "/")(implicit api: Api[F], log: LogWriter[F])
    extends WebhookBot[F](api, url, path)
    with BotSkeleton[F] {
  override def onMessage(msg: Message): F[Unit] = {
    val x: OptionT[F, Unit] = for {
      _    <- OptionT.liftF(log.trace(s"A message arrived: $msg"))
      text <- OptionT.fromOption[F](msg.text)
      _    <- OptionT.liftF(log.info(s"A message arrived with content: $text"))
      _    <- OptionT(botLogic(Async[F], api, log)(msg))
    } yield ()
    x.getOrElseF(log.debug(s"Input message produced no result: $msg"))
  }
}

trait BotSkeleton[F[_]] extends DefaultActions[F] {

  // Configuration values /////////////////////////////////////////////////////
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = ResourceAccess.fromResources[F]
  val ignoreMessagePrefix: Option[String]                                 = Some("!")
  val inputTimeout: Option[Duration]                                      = Some(5.minute)
  val disableForward: Boolean                                             = true

  // Reply to Messages ////////////////////////////////////////////////////////

  def messageRepliesDataF(implicit applicativeF: Applicative[F], log: LogWriter[F]): F[List[ReplyBundleMessage[F]]] =
    log.debug("Empty message reply data") *> List.empty[ReplyBundleMessage[F]].pure[F]
  def commandRepliesDataF(implicit asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    log.debug("Empty command reply data") *> List.empty[ReplyBundleCommand[F]].pure[F]

  // Bot logic //////////////////////////////////////////////////////////////////////////////

  def messageLogic(implicit asyncF: Async[F], api: Api[F], log: LogWriter[F]): (Message) => F[Option[List[Message]]] =
    (msg: Message) =>
      for {
        messageRepliesData <- messageRepliesDataF
        replies <- messageRepliesData
          .find(MessageMatches.doesMatch(_, msg, ignoreMessagePrefix))
          .filter(_ => Timeout.isWithinTimeout(msg.date, inputTimeout) && FilteringForward.filter(msg, disableForward))
          .traverse(ReplyBundle.computeReplyBundle[F](_, msg))
      } yield replies

  def commandLogic(implicit asyncF: Async[F], api: Api[F], log: LogWriter[F]): (Message) => F[Option[List[Message]]] =
    (msg: Message) =>
      for {
        commandRepliesData <- commandRepliesDataF
        commandMatch = for {
          text   <- msg.text
          result <- commandRepliesData.find(rbc => text.startsWith("/" + rbc.trigger.command))
        } yield result
        commands <- commandMatch
          .traverse(
            ReplyBundle.computeReplyBundle[F](_, msg)
          )
      } yield commands

  def botLogic(implicit asyncF: Async[F], api: Api[F], log: LogWriter[F]): (Message) => F[Option[List[Message]]] =
    (msg: Message) =>
      for {
        messagesOpt <- messageLogic(asyncF, api, log)(msg)
        commandsOpt <- commandLogic(asyncF, api, log)(msg)
      } yield SemigroupK[Option].combineK(messagesOpt, commandsOpt)
}
