package com.benkio.telegrambotinfrastructure

import com.benkio.telegrambotinfrastructure.model.{MessageLengthTrigger, NewMemberTrigger, ReplyBundleMessage, TextTrigger, TextTriggerValue}
import telegramium.bots.Message

trait MessageMatches

object ContainsOnce extends MessageMatches
object ContainsAll  extends MessageMatches

object MessageMatches {

  def doesMatch[F[_]](
      replyMessageBundle: ReplyBundleMessage[F],
      message: Message,
      ignoreMessagePrefix: Option[String]
  ): Boolean =
    (ignoreMessagePrefix, replyMessageBundle.matcher, replyMessageBundle.trigger) match {
      case (Some(prefix), _, _) if message.text.isDefined && message.text.get.startsWith(prefix) => false
      case (_, ContainsOnce, TextTrigger(triggers @ _*))
          if message.text.isDefined && triggers.exists(TextTriggerValue.matchValue(_, message.text.get.toLowerCase())) =>
        true
      case (_, ContainsAll, TextTrigger(triggers @ _*))
          if message.text.isDefined && triggers.forall(TextTriggerValue.matchValue(_, message.text.get.toLowerCase())) =>
        true
      case (_, _, MessageLengthTrigger(messageLength)) if message.text.isDefined && message.text.get.size >= messageLength => true
      case (_, _, NewMemberTrigger()) if message.newChatMembers.nonEmpty => true
      case _                                                                                => false
    }
}
