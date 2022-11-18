package com.benkio.telegrambotinfrastructure.model

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.messagefiltering.ContainsOnce
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import telegramium.bots.Message

sealed trait ReplyBundle[F[_]] {

  def trigger: Trigger
  def mediafiles: List[MediaFile]
  def text: Option[TextReply[F]]
  def replySelection: ReplySelection
}

final case class ReplyBundleMessage[F[_]](
    trigger: MessageTrigger,
    mediafiles: List[MediaFile],
    text: Option[TextReply[F]],
    matcher: MessageMatches,
    replySelection: ReplySelection
) extends ReplyBundle[F]

object ReplyBundleMessage {
  def apply[F[_]](
      trigger: MessageTrigger,
      mediafiles: List[MediaFile] = List.empty[MediaFile],
      text: Option[TextReply[F]] = None,
      matcher: MessageMatches = ContainsOnce,
      replySelection: ReplySelection = SelectAll
  ): ReplyBundleMessage[F] = new ReplyBundleMessage[F](
    trigger = trigger,
    mediafiles = mediafiles,
    text = text,
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
    text: Option[TextReply[F]],
    replySelection: ReplySelection
) extends ReplyBundle[F]

object ReplyBundleCommand {
  def apply[F[_]](
      trigger: CommandTrigger,
      mediafiles: List[MediaFile] = List.empty[MediaFile],
      text: Option[TextReply[F]] = None,
      replySelection: ReplySelection = SelectAll
  ): ReplyBundleCommand[F] = new ReplyBundleCommand[F](
    trigger = trigger,
    mediafiles = mediafiles,
    text = text,
    replySelection = replySelection
  )
}

object ReplyBundle {

  implicit def orderingInstance[F[_]]: Ordering[ReplyBundle[F]] =
    Trigger.orderingInstance.contramap(_.trigger)

  private def replyBundleToData[F[_]](replyBundle: ReplyBundle[F], f: Boolean): List[Reply] =
    if (f) replyBundle.mediafiles ++ replyBundle.text.toList
    else List.empty

  def computeReplyBundle[F[_]](replyBundle: ReplyBundle[F], message: Message, filter: F[Boolean])(implicit
      replyAction: Action[F],
      syncF: Sync[F]
  ): F[List[Message]] = for {
    f <- filter
    dataToSend = replyBundleToData[F](replyBundle, f)
    replies <- replyBundle.replySelection.logic(dataToSend)
    result  <- replies.traverse[F, List[Message]](replyAction(_)(message))
  } yield result.flatten
}
