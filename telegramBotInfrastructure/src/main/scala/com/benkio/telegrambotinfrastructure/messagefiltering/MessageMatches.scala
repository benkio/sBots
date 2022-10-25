package com.benkio.telegrambotinfrastructure.messagefiltering

import com.benkio.telegrambotinfrastructure.model.LeftMemberTrigger
import com.benkio.telegrambotinfrastructure.model.MessageLengthTrigger
import com.benkio.telegrambotinfrastructure.model.NewMemberTrigger
import com.benkio.telegrambotinfrastructure.model.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import com.benkio.telegrambotinfrastructure.model.TextTriggerValue
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
    (ignoreMessagePrefix, replyMessageBundle.matcher, replyMessageBundle.trigger, message.text) match {
      case (Some(prefix), _, _, Some(messageText)) if messageText.startsWith(prefix) => false
      case (_, ContainsOnce, TextTrigger(triggers @ _*), Some(messageText))
          if triggers.exists(TextTriggerValue.matchValue(_, messageText.toLowerCase())) =>
        true
      case (_, ContainsAll, TextTrigger(triggers @ _*), Some(messageText))
          if triggers.forall(TextTriggerValue.matchValue(_, messageText.toLowerCase())) =>
        true
      case (_, _, MessageLengthTrigger(messageLength), Some(messageText)) if messageText.size >= messageLength => true
      case (_, _, _: NewMemberTrigger.type, _) if message.newChatMembers.nonEmpty                              => true
      case (_, _, _: LeftMemberTrigger.type, _) if message.leftChatMember.nonEmpty                             => true
      case _                                                                                                   => false
    }
}
