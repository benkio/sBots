package com.benkio.telegramBotInfrastructure.model

import com.benkio.telegramBotInfrastructure.{ContainsOnce, MessageMatches}
import info.mukel.telegrambot4s.models.Message
import scala.concurrent.{Future, ExecutionContext}
import com.benkio.telegramBotInfrastructure.default.Actions.Action

sealed trait ReplyBundle {

  def triggers : List[String]
  def mediafiles : List[MediaFile]
  def text : List[Text]
}

final case class ReplyBundleMessage(
  triggers: List[String],
  mediafiles: List[MediaFile] = List.empty[MediaFile],
  text : List[Text] = List.empty[Text],
  replyMessageId : Option[Int] = None,
  matcher: MessageMatches = ContainsOnce
) extends ReplyBundle

final case class ReplyBundleCommand(
  triggers : List[String],
  mediafiles: List[MediaFile],
  text : List[Text],
) extends ReplyBundle

object ReplyBundle {

  def computeReplyBundle(replyBundle : ReplyBundle, message : Message)(
    implicit audioAction : Action[Mp3File],
    gifAction : Action[GifFile],
    photoAction : Action[PhotoFile],
    textAction : Action[Text],
    ec : ExecutionContext
  ) : Future[List[Message]] = for {
    m1 <- Future.traverse(replyBundle.mediafiles)(Reply.toMessageReply(_, message))
    m2 <- Future.traverse(replyBundle.text)(Reply.toMessageReply(_, message))
  } yield m1 ++ m2
}
