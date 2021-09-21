package com.benkio.telegrambotinfrastructure.model

import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.ContainsOnce
import com.benkio.telegrambotinfrastructure.MessageMatches
import telegramium.bots.Message

sealed trait ReplyBundle {

  def trigger: Trigger
  def mediafiles: List[MediaFile]
  def text: TextReply
  def replySelection: ReplySelection
}

final case class ReplyBundleMessage(
    trigger: MessageTrigger,
    mediafiles: List[MediaFile] = List.empty[MediaFile],
    text: TextReply = TextReply(_ => List.empty[List[String]], false),
    matcher: MessageMatches = ContainsOnce,
    replySelection: ReplySelection = SelectAll
) extends ReplyBundle

final case class ReplyBundleCommand(
    trigger: CommandTrigger,
    mediafiles: List[MediaFile] = List.empty[MediaFile],
    text: TextReply = TextReply(_ => List.empty[List[String]], false),
    replySelection: ReplySelection = SelectAll
) extends ReplyBundle

object ReplyBundle {

  def computeReplyBundle[F[_]](replyBundle: ReplyBundle, message: Message)(implicit
      replyAction: Action[Reply, F],
      syncF: Sync[F]
  ): F[List[Message]] = {
    val dataToSend =
      if (replyBundle.text.text(message).isEmpty)
        replyBundle.mediafiles
      else replyBundle.mediafiles :+ replyBundle.text
    for {
      replies <- replyBundle.replySelection.logic(dataToSend)
      result  <- replies.traverse[F, List[Message]](replyAction(_)(message))
    } yield result.flatten
  }
}
