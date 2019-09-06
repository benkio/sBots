package com.benkio.telegramBotInfrastructure.model

import com.benkio.telegramBotInfrastructure.{ContainsOnce, MessageMatches}

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
