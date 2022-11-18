package com.benkio.telegrambotinfrastructure.model

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.messagefiltering.{ContainsOnce, MessageMatches}
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

  def prettyPrint[F[_]](rbm: ReplyBundleMessage[F])(implicit triggerShow: Show[Trigger]): String = {
    val triggerStrings: List[String]   = triggerShow.show(rbm.trigger).split('\n').toList
    val mediaFilesString: List[String] = rbm.mediafiles.map(_.show)
    val result = mediaFilesString
      .zipAll(triggerStrings, "", "")
      .map { case (mfs, trs) =>
        s"${mfs.padTo(25, ' ')} | $trs"
      }
      .mkString("\n")
    ("-" * 50) + s"\n${result}\n" + ("-" * 50) + "\n"
  }
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

  implicit def orderingInstance[F[_]]: Ordering[ReplyBundle[F]] =
    Trigger.orderingInstance.contramap(_.trigger)

  private def replyBundleToData[F[_]](replyBundle: ReplyBundle[F], textReplies: List[String], f: Boolean): List[Reply] =
    (textReplies, f) match {
      case (_, false) => List.empty
      case (Nil, _)   => replyBundle.mediafiles
      case _          => replyBundle.mediafiles :+ replyBundle.text
    }

  def computeReplyBundle[F[_]](replyBundle: ReplyBundle[F], message: Message, filter: F[Boolean])(implicit
      replyAction: Action[F],
      syncF: Sync[F]
  ): F[List[Message]] = for {
    _ <- println(s"computeReplyBundle here").pure[F]
    f           <- filter
    textReplies <- replyBundle.text.text(message)
    dataToSend = replyBundleToData[F](replyBundle, textReplies, f)
    replies <- replyBundle.replySelection.logic(dataToSend)
    result  <- replies.traverse[F, List[Message]](replyAction(_)(message))
  } yield result.flatten
}
