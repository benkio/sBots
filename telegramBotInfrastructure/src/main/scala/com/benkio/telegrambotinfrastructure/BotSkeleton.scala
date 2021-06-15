package com.benkio.telegrambotinfrastructure

import com.benkio.telegrambotinfrastructure.botCapabilities.ResourceSource
import com.benkio.telegrambotinfrastructure.default.DefaultActions
import com.benkio.telegrambotinfrastructure.model.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.Timeout
import telegramium.bots.high._
import telegramium.bots.Message
import cats.effect._
import cats._
import cats.implicits._

import scala.concurrent.duration._

abstract class BotSkeleton()(implicit api: Api[IO], parallel: Parallel[IO], time: Timer[IO])
    extends LongPollBot[IO](api)
    with DefaultActions {

  // Configuration values /////////////////////////////////////////////////////
  val resourceSource: ResourceSource
  val ignoreMessagePrefix: Option[String] = Some("!")
  val inputTimeout: Option[Duration]      = Some(5.minute)

  // Reply to Messages ////////////////////////////////////////////////////////

  lazy val messageRepliesData: List[ReplyBundleMessage] = List.empty[ReplyBundleMessage]
  lazy val commandRepliesData: List[ReplyBundleCommand] = List.empty[ReplyBundleCommand]

  // Bot logic //////////////////////////////////////////////////////////////////////////////

  val messageLogic: (Message, String) => Option[IO[List[Message]]] = (msg: Message, text: String) =>
    messageRepliesData
      .find(MessageMatches.doesMatch(_, text, ignoreMessagePrefix))
      .filter(_ => Timeout.isWithinTimeout(msg.date, inputTimeout))
      .map(rbm => ReplyBundle.computeReplyBundle[IO](rbm, msg))

  val commandLogic: (Message, String) => Option[IO[List[Message]]] = (msg: Message, text: String) =>
    commandRepliesData
      .find(rbc => text.startsWith("/" + rbc.trigger.command))
      .map(
        ReplyBundle.computeReplyBundle[IO](_, msg)
      )

  val botLogic: (Message, String) => Option[IO[List[Message]]] = (msg: Message, text: String) =>
    SemigroupK[Option].combineK(messageLogic(msg, text), commandLogic(msg, text))

  override def onMessage(msg: Message): IO[Unit] = {
    val x: Option[IO[Unit]] = for {
      text   <- msg.text
      theENd <- botLogic(msg, text)
    } yield theENd.void
    x.getOrElse(Monad[IO].unit)
  }
}
