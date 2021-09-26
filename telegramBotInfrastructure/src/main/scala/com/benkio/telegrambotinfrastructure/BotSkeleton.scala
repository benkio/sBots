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

abstract class BotSkeleton[F[_]: Parallel: Async](implicit api: Api[F])
    extends LongPollBot[F](api)
    with DefaultActions {

  // Configuration values /////////////////////////////////////////////////////
  val resourceSource: ResourceSource
  val ignoreMessagePrefix: Option[String] = Some("!")
  val inputTimeout: Option[Duration]      = Some(5.minute)

  // Reply to Messages ////////////////////////////////////////////////////////

  lazy val messageRepliesDataF: F[List[ReplyBundleMessage]] = List.empty[ReplyBundleMessage].pure[F]
  lazy val commandRepliesDataF: F[List[ReplyBundleCommand]] = List.empty[ReplyBundleCommand].pure[F]

  // Bot logic //////////////////////////////////////////////////////////////////////////////

  val messageLogic: (Message, String) => F[Option[List[Message]]] = (msg: Message, text: String) =>
    for {
      messageRepliesData <- messageRepliesDataF
      replies <- messageRepliesData
        .find(MessageMatches.doesMatch(_, text, ignoreMessagePrefix))
        .filter(_ => Timeout.isWithinTimeout(msg.date, inputTimeout))
        .traverse(ReplyBundle.computeReplyBundle[F](_, msg))
    } yield replies

  val commandLogic: (Message, String) => F[Option[List[Message]]] = (msg: Message, text: String) =>
    for {
      commandRepliesData <- commandRepliesDataF
      commands <- commandRepliesData
        .find(rbc => text.startsWith("/" + rbc.trigger.command))
        .traverse(
          ReplyBundle.computeReplyBundle[F](_, msg)
        )
    } yield commands

  val botLogic: (Message, String) => F[Option[List[Message]]] = (msg: Message, text: String) =>
    for {
      messagesOpt <- messageLogic(msg, text)
      commandsOpt <- commandLogic(msg, text)
    } yield SemigroupK[Option].combineK(messagesOpt, commandsOpt)

  override def onMessage(msg: Message): F[Unit] = {
    val x: OptionT[F, Unit] = for {
      text <- OptionT.fromOption[F](msg.text)
      _    <- OptionT(botLogic(msg, text))
    } yield ()
    x.getOrElse(())
  }
}
