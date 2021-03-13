package com.benkio.telegramBotInfrastructure.model

import cats.effect._
import cats.implicits._
import com.benkio.telegramBotInfrastructure.ContainsOnce
import com.benkio.telegramBotInfrastructure.MessageMatches
import telegramium.bots.Message
import com.benkio.telegramBotInfrastructure.default.Actions.Action

sealed trait ReplyBundle {

  def trigger: Trigger
  def mediafiles: List[MediaFile]
  def text: TextReply
  def replySelection: ReplySelection
}

final case class ReplyBundleMessage(
    trigger: MessageTrigger,
    mediafiles: List[MediaFile] = List.empty[MediaFile],
    text: TextReply = TextReply(_ => List.empty[String], false),
    matcher: MessageMatches = ContainsOnce,
    replySelection: ReplySelection = SelectAll
) extends ReplyBundle

final case class ReplyBundleCommand(
    trigger: CommandTrigger,
    mediafiles: List[MediaFile] = List.empty[MediaFile],
    text: TextReply = TextReply(_ => List.empty[String], false),
    replySelection: ReplySelection = SelectAll
) extends ReplyBundle

object ReplyBundle {

  def computeReplyBundle[F[_]](replyBundle: ReplyBundle, message: Message)(
      implicit replyAction: Action[Reply, F],
      syncF: Sync[F]
  ): F[List[Message]] =
    for {
      replies <- replyBundle.replySelection.logic(replyBundle.mediafiles :+ replyBundle.text)
      result  <- replies.traverse[F, Message](replyAction(_)(message))
    } yield result

}
