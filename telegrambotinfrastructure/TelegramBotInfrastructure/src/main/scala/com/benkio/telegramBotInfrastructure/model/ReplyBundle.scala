package com.benkio.telegramBotInfrastructure.model

import com.benkio.telegramBotInfrastructure.{ContainsOnce, MessageMatches}
import info.mukel.telegrambot4s.models.Message
import scala.concurrent.{Future, ExecutionContext}
import com.benkio.telegramBotInfrastructure.default.Actions.Action

sealed trait ReplyBundle {

  def triggers : List[String]
  def mediafiles : List[MediaFile]
  def text : List[Text]
  def replySelection : ReplySelection
}

final case class ReplyBundleMessage(
  triggers: List[String],
  mediafiles: List[MediaFile] = List.empty[MediaFile],
  text : List[Text] = List.empty[Text],
  replyMessageId : Option[Int] = None,
  matcher: MessageMatches = ContainsOnce,
  replySelection : ReplySelection = SelectAll
) extends ReplyBundle

final case class ReplyBundleCommand(
  triggers : List[String],
  mediafiles: List[MediaFile],
  text : List[Text],
  replySelection : ReplySelection = SelectAll
) extends ReplyBundle

object ReplyBundle {

  def computeReplyBundle(replyBundle : ReplyBundle, message : Message)(
    implicit audioAction : Action[Mp3File],
    gifAction : Action[GifFile],
    photoAction : Action[PhotoFile],
    textAction : Action[Text],
    ec : ExecutionContext
  ) : Future[List[Message]] = {
    val replies : List[Reply] = replyBundle.replySelection.logic(replyBundle.mediafiles ++ replyBundle.text)
    Future.traverse(replies)(Reply.toMessageReply(_, message))
  }

}
