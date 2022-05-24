package com.benkio.telegrambotinfrastructure

import cats._
import cats.data.OptionT
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceSource
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
    with BotSkeleton {
  override def onMessage(msg: Message): F[Unit] = {
    val x: OptionT[F, Unit] = for {
      _    <- OptionT.liftF(log.trace(s"A message arrived: $msg"))
      text <- OptionT.fromOption[F](msg.text)
      _    <- OptionT.liftF(log.info(s"A message arrived with content: $text"))
      _    <- OptionT(botLogic[F](Async[F], api)(msg))
    } yield ()
    x.getOrElseF(log.debug(s"Input message produced no result: $msg"))
  }
}

abstract class BotSkeletonWebhook[F[_]: Async](url: String, path: String = "/")(implicit api: Api[F], log: LogWriter[F])
    extends WebhookBot[F](api, url, path)
    with BotSkeleton {
  override def onMessage(msg: Message): F[Unit] = {
    val x: OptionT[F, Unit] = for {
      _    <- OptionT.liftF(log.trace(s"A message arrived: $msg"))
      text <- OptionT.fromOption[F](msg.text)
      _    <- OptionT.liftF(log.info(s"A message arrived with content: $text"))
      _    <- OptionT(botLogic[F](Async[F], api)(msg))
    } yield ()
    x.getOrElseF(log.debug(s"Input message produced no result: $msg"))
  }
}

trait BotSkeleton extends DefaultActions {

  // Configuration values /////////////////////////////////////////////////////
  val resourceSource: ResourceSource
  val ignoreMessagePrefix: Option[String] = Some("!")
  val inputTimeout: Option[Duration]      = Some(5.minute)
  val disableForward: Boolean             = true

  // Reply to Messages ////////////////////////////////////////////////////////

  def messageRepliesDataF[F[_]: Applicative]: F[List[ReplyBundleMessage[F]]] = List.empty[ReplyBundleMessage[F]].pure[F]
  def commandRepliesDataF[F[_]: Async]: F[List[ReplyBundleCommand[F]]]       = List.empty[ReplyBundleCommand[F]].pure[F]

  // Bot logic //////////////////////////////////////////////////////////////////////////////

  def messageLogic[F[_]: Async: Api]: (Message) => F[Option[List[Message]]] = (msg: Message) =>
    for {
      messageRepliesData <- messageRepliesDataF[F]
      replies <- messageRepliesData
        .find(MessageMatches.doesMatch(_, msg, ignoreMessagePrefix))
        .filter(_ => Timeout.isWithinTimeout(msg.date, inputTimeout) && FilteringForward.filter(msg, disableForward))
        .traverse(ReplyBundle.computeReplyBundle[F](_, msg))
    } yield replies

  def commandLogic[F[_]: Async: Api]: (Message) => F[Option[List[Message]]] = (msg: Message) =>
    for {
      commandRepliesData <- commandRepliesDataF[F]
      commandMatch = for {
        text   <- msg.text
        result <- commandRepliesData.find(rbc => text.startsWith("/" + rbc.trigger.command))
      } yield result
      commands <- commandMatch
        .traverse(
          ReplyBundle.computeReplyBundle[F](_, msg)
        )
    } yield commands

  def botLogic[F[_]: Async: Api]: (Message) => F[Option[List[Message]]] = (msg: Message) =>
    for {
      messagesOpt <- messageLogic[F](Async[F], implicitly[Api[F]])(msg)
      commandsOpt <- commandLogic[F](Async[F], implicitly[Api[F]])(msg)
    } yield SemigroupK[Option].combineK(messagesOpt, commandsOpt)
}
