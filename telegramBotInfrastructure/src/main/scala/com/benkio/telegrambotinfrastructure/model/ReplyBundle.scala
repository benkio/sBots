package com.benkio.telegrambotinfrastructure.model

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.ContainsOnce
import com.benkio.telegrambotinfrastructure.MessageMatches
import telegramium.bots.Message

sealed trait ReplyBundle[F[_]] {

  def trigger: Trigger
  def mediafiles: List[MediaFile]
  def text: TextReply[F]
  def replySelection: ReplySelection
}

final case class ReplyBundleMessage[F[_]](
    trigger: MessageTrigger,
    mediafiles: List[MediaFile],
    text: TextReply[F],
    matcher: MessageMatches,
    replySelection: ReplySelection
) extends ReplyBundle[F]

object ReplyBundleMessage {
  def apply[F[_]: Applicative](
      trigger: MessageTrigger,
      mediafiles: List[MediaFile] = List.empty[MediaFile],
      text: Option[TextReply[F]] = None,
      matcher: MessageMatches = ContainsOnce,
      replySelection: ReplySelection = SelectAll
  ): ReplyBundleMessage[F] = ReplyBundleMessage[F](
    trigger = trigger,
    mediafiles = mediafiles,
    text = text.getOrElse(TextReply(_ => Applicative[F].pure(List.empty[String]), false)),
    matcher = matcher,
    replySelection = replySelection
  )
}

final case class ReplyBundleCommand[F[_]](
    trigger: CommandTrigger,
    mediafiles: List[MediaFile],
    text: TextReply[F],
    replySelection: ReplySelection
) extends ReplyBundle[F]

object ReplyBundleCommand {
  def apply[F[_]: Applicative](
      trigger: CommandTrigger,
      mediafiles: List[MediaFile] = List.empty[MediaFile],
      text: Option[TextReply[F]] = None,
      replySelection: ReplySelection = SelectAll
  ): ReplyBundleCommand[F] = ReplyBundleCommand[F](
    trigger = trigger,
    mediafiles = mediafiles,
    text = text.getOrElse(TextReply(_ => Applicative[F].pure(List.empty[String]), false)),
    replySelection = replySelection
  )
}

object ReplyBundle {

  implicit def ordering[F[_]]: Ordering[ReplyBundle[F]] =
    Trigger.ordering.contramap(_.trigger)

  def computeReplyBundle[F[_]](replyBundle: ReplyBundle[F], message: Message)(implicit
      replyAction: Action[Reply, F],
      syncF: Sync[F]
  ): F[List[Message]] = for {
    textReplies <- replyBundle.text.text(message)
    dataToSend =
      if (textReplies.isEmpty)
        replyBundle.mediafiles
      else replyBundle.mediafiles :+ replyBundle.text
    replies <- replyBundle.replySelection.logic(dataToSend)
    result  <- replies.traverse[F, List[Message]](replyAction(_)(message))
  } yield result.flatten
}
