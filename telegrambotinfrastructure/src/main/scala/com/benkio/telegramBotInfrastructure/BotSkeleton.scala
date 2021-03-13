package com.benkio.telegramBotInfrastructure

import com.benkio.telegramBotInfrastructure.botCapabilities.FileSystem
import com.benkio.telegramBotInfrastructure.botCapabilities.ResourceSource
import com.benkio.telegramBotInfrastructure.default.DefaultActions
import com.benkio.telegramBotInfrastructure.model.ReplyBundle
import com.benkio.telegramBotInfrastructure.model.ReplyBundleMessage
import com.benkio.telegramBotInfrastructure.model.ReplyBundleCommand
import com.benkio.telegramBotInfrastructure.model.Timeout
import telegramium.bots.high._
import telegramium.bots.Message
import cats.effect._
import cats._
import cats.implicits._

import scala.concurrent.duration._

class BotSkeleton[F[_]: Sync: Timer: Parallel]()(implicit api: Api[F])
    extends LongPollBot[F](api)
    with DefaultActions
    with Configurations {

  // Configuration values /////////////////////////////////////////////////////
  val resourceSource: ResourceSource      = FileSystem
  val ignoreMessagePrefix: Option[String] = Some("!")
  val inputTimeout: Option[Duration]      = Some(5.minute)

  // Reply to Messages ////////////////////////////////////////////////////////

  lazy val messageRepliesData: List[ReplyBundleMessage] = List.empty[ReplyBundleMessage]
  lazy val commandRepliesData: List[ReplyBundleCommand] = List.empty[ReplyBundleCommand]

  // Bot logic //////////////////////////////////////////////////////////////////////////////

  val messageLogic: (Message, String) => Option[F[Unit]] = (msg: Message, text: String) =>
    messageRepliesData
      .find(MessageMatches.doesMatch(_, text, ignoreMessagePrefix))
      .filter(_ => Timeout.isWithinTimeout(msg.date, inputTimeout))
      .map(rbm => ReplyBundle.computeReplyBundle(rbm, msg).void)

  val commandLogic: (Message, String) => Option[F[Unit]] = (msg: Message, text: String) =>
    commandRepliesData
      .find(rbc => text.startsWith("/" + rbc.trigger.command))
      .map(
        ReplyBundle.computeReplyBundle(_, msg).void
      )

  val botLogic: (Message, String) => Option[F[Unit]] = (msg: Message, text: String) =>
    SemigroupK[Option].combineK(messageLogic(msg, text), commandLogic(msg, text))

  override def onMessage(msg: Message): F[Unit] = {
    val x: Option[F[Unit]] = (for {
      text <- msg.text
      _    <- botLogic(msg, text)
    } yield Sync[F].unit)
    x.getOrElse(Monad[F].unit)
  }
}
