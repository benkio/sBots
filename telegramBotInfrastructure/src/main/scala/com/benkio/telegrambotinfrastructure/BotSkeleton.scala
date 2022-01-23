package com.benkio.telegrambotinfrastructure

import cats._
import cats.data.OptionT
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botCapabilities.ResourceSource
import com.benkio.telegrambotinfrastructure.default.DefaultActions
import com.benkio.telegrambotinfrastructure.model.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.Timeout
import telegramium.bots.Message
import telegramium.bots.high._

import scala.concurrent.duration._

abstract class BotSkeletonPolling[F[_]: Parallel: Async](implicit api: Api[F])
    extends LongPollBot[F](api)
    with BotSkeleton {
  override def onMessage(msg: Message): F[Unit] = {
    val x: OptionT[F, Unit] = for {
      text <- OptionT.fromOption[F](msg.text)
      _    <- OptionT(botLogic[F](Async[F], api)(msg, text))
    } yield ()
    x.getOrElse(())
  }
}

abstract class BotSkeletonWebhook[F[_]: Async](api: Api[F], url: String, path: String = "/")
    extends WebhookBot[F](api, url, path)
    with BotSkeleton {
  override def onMessage(msg: Message): F[Unit] = {
    val x: OptionT[F, Unit] = for {
      text <- OptionT.fromOption[F](msg.text)
      _    <- OptionT(botLogic[F](Async[F], api)(msg, text))
    } yield ()
    x.getOrElse(())
  }
}

trait BotSkeleton extends DefaultActions {

  // Configuration values /////////////////////////////////////////////////////
  val resourceSource: ResourceSource
  val ignoreMessagePrefix: Option[String] = Some("!")
  val inputTimeout: Option[Duration]      = Some(5.minute)

  // Reply to Messages ////////////////////////////////////////////////////////

  def messageRepliesDataF[F[_]: Applicative]: F[List[ReplyBundleMessage[F]]] = List.empty[ReplyBundleMessage[F]].pure[F]
  def commandRepliesDataF[F[_]: Async]: F[List[ReplyBundleCommand[F]]]       = List.empty[ReplyBundleCommand[F]].pure[F]

  // Bot logic //////////////////////////////////////////////////////////////////////////////

  def messageLogic[F[_]: Async: Api]: (Message, String) => F[Option[List[Message]]] = (msg: Message, text: String) =>
    for {
      messageRepliesData <- messageRepliesDataF[F]
      replies <- messageRepliesData
        .find(MessageMatches.doesMatch(_, text, ignoreMessagePrefix))
        .filter(_ => Timeout.isWithinTimeout(msg.date, inputTimeout))
        .traverse(ReplyBundle.computeReplyBundle[F](_, msg))
    } yield replies

  def commandLogic[F[_]: Async: Api]: (Message, String) => F[Option[List[Message]]] = (msg: Message, text: String) =>
    for {
      commandRepliesData <- commandRepliesDataF[F]
      commands <- commandRepliesData
        .find(rbc => text.startsWith("/" + rbc.trigger.command))
        .traverse(
          ReplyBundle.computeReplyBundle[F](_, msg)
        )
    } yield commands

  def botLogic[F[_]: Async: Api]: (Message, String) => F[Option[List[Message]]] = (msg: Message, text: String) =>
    for {
      messagesOpt <- messageLogic[F](Async[F], implicitly[Api[F]])(msg, text)
      commandsOpt <- commandLogic[F](Async[F], implicitly[Api[F]])(msg, text)
    } yield SemigroupK[Option].combineK(messagesOpt, commandsOpt)
}
