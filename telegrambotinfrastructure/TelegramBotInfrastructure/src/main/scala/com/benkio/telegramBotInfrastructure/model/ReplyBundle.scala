package com.benkio.telegramBotInfrastructure.model

import com.benkio.telegramBotInfrastructure.ContainsOnce
import com.benkio.telegramBotInfrastructure.MessageMatches
import info.mukel.telegrambot4s.models.Message
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
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
    text: TextReply = TextReply((m: Message) => List.empty[String], false),
    matcher: MessageMatches = ContainsOnce,
    replySelection: ReplySelection = SelectAll
) extends ReplyBundle

final case class ReplyBundleCommand(
    trigger: CommandTrigger,
    mediafiles: List[MediaFile] = List.empty[MediaFile],
    text: TextReply = TextReply((m: Message) => List.empty[String], false),
    replySelection: ReplySelection = SelectAll
) extends ReplyBundle

object ReplyBundle {

  def computeReplyBundle(replyBundle: ReplyBundle, message: Message)(
      implicit audioAction: Action[Mp3File],
      gifAction: Action[GifFile],
      photoAction: Action[PhotoFile],
      textAction: Action[TextReply],
      ec: ExecutionContext
  ): Future[List[Message]] = {
    val replies: List[Reply] = replyBundle.replySelection.logic(replyBundle.mediafiles :+ replyBundle.text)
    Future.traverse(replies)(Reply.toMessageReply(_, message))
  }

}
