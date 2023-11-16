package com.benkio.telegrambotinfrastructure.model

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.messagefiltering.ContainsOnce
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import telegramium.bots.Message
import com.benkio.telegrambotinfrastructure.model.Reply.prettyPrint

sealed trait ReplyBundle[F[_]] {

  def trigger: Trigger
  def reply: Reply[F]
  def replySelection: ReplySelection
}

final case class ReplyBundleMessage[F[_]](
    trigger: MessageTrigger,
    reply: Reply[F],
    matcher: MessageMatches,
    replySelection: ReplySelection
) extends ReplyBundle[F]

object ReplyBundleMessage {
  def apply[F[_]](
      trigger: MessageTrigger,
      reply: Reply[F],
      matcher: MessageMatches = ContainsOnce,
      replySelection: ReplySelection = SelectAll
  ): ReplyBundleMessage[F] = new ReplyBundleMessage[F](
    trigger = trigger,
    reply = reply,
    matcher = matcher,
    replySelection = replySelection
  )

  def prettyPrint[F[_]: Applicative](rbm: ReplyBundleMessage[F])(implicit triggerShow: Show[Trigger]): F[String] = {
    val triggerStrings: List[String]   = triggerShow.show(rbm.trigger).split('\n').toList
    val replyString: F[List[String]] = rbm.reply.prettyPrint
    val result = replyString.map(x =>
      x.zipAll(triggerStrings, "", "")
      .map { case (mfs, trs) =>
        s"${mfs.padTo(25, ' ')} | $trs"
      }
      .mkString("\n")
    )
    result.map(r => ("-" * 50) + s"\n$r\n" + ("-" * 50) + "\n")
  }
}

final case class ReplyBundleCommand[F[_]](
    trigger: CommandTrigger,
    reply: Reply[F],
    replySelection: ReplySelection
) extends ReplyBundle[F]

object ReplyBundleCommand {
  def apply[F[_]](
      trigger: CommandTrigger,
      reply: Reply[F],
      replySelection: ReplySelection = SelectAll
  ): ReplyBundleCommand[F] = new ReplyBundleCommand[F](
    trigger = trigger,
    reply = reply,
    replySelection = replySelection
  )
}

object ReplyBundle {

  implicit def orderingInstance[F[_]]: Ordering[ReplyBundle[F]] =
    Trigger.orderingInstance.contramap(_.trigger)

  def computeReplyBundle[F[_]](replyBundle: ReplyBundle[F], message: Message, filter: F[Boolean])(implicit
      replyAction: Action[F],
      syncF: Sync[F]
  ): F[List[Message]] = for {
    dataToReply <- syncF.ifM(filter)(ifTrue = syncF.pure(replyBundle.reply), ifFalse = syncF.raiseError(new Exception(s"No replies for the given message: $message")))
    replies <- replyBundle.replySelection.logic(dataToReply, message)
    result  <- replies.traverse[F, List[Message]](replyAction(_)(message))
  } yield result.flatten
}
