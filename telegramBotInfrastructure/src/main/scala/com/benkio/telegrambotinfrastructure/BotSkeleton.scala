package com.benkio.telegrambotinfrastructure

import cats._
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

abstract class BotSkeleton[F[_]: Timer: Parallel: Effect]()(implicit api: Api[F])
    extends LongPollBot[F](api)
    with DefaultActions {

  // Configuration values /////////////////////////////////////////////////////
  val resourceSource: ResourceSource
  val ignoreMessagePrefix: Option[String] = Some("!")
  val inputTimeout: Option[Duration]      = Some(5.minute)

  // Reply to Messages ////////////////////////////////////////////////////////

  lazy val messageRepliesData: List[ReplyBundleMessage] = List.empty[ReplyBundleMessage]
  lazy val commandRepliesData: List[ReplyBundleCommand] = List.empty[ReplyBundleCommand]

  // Bot logic //////////////////////////////////////////////////////////////////////////////

  val messageLogic: (Message, String) => Option[F[List[Message]]] = (msg: Message, text: String) =>
    messageRepliesData
      .find(MessageMatches.doesMatch(_, text, ignoreMessagePrefix))
      .filter(_ => Timeout.isWithinTimeout(msg.date, inputTimeout))
      .map(rbm => ReplyBundle.computeReplyBundle[F](rbm, msg))

  val commandLogic: (Message, String) => Option[F[List[Message]]] = (msg: Message, text: String) =>
    commandRepliesData
      .find(rbc => text.startsWith("/" + rbc.trigger.command))
      .map(
        ReplyBundle.computeReplyBundle[F](_, msg)
      )

  val botLogic: (Message, String) => Option[F[List[Message]]] = (msg: Message, text: String) =>
    SemigroupK[Option].combineK(messageLogic(msg, text), commandLogic(msg, text))

  override def onMessage(msg: Message): F[Unit] = {
    val x: Option[F[Unit]] = for {
      text   <- msg.text
      theENd <- botLogic(msg, text)
    } yield theENd.void
    x.getOrElse(Monad[F].unit)
  }
}
