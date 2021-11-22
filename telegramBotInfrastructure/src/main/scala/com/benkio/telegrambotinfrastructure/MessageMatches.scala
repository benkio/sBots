package com.benkio.telegrambotinfrastructure

import com.benkio.telegrambotinfrastructure.model.MessageLengthTrigger
import com.benkio.telegrambotinfrastructure.model.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import com.benkio.telegrambotinfrastructure.model.TextTriggerValue

trait MessageMatches

object ContainsOnce extends MessageMatches
object ContainsAll  extends MessageMatches

object MessageMatches {

  def doesMatch(
      replyMessageBundle: ReplyBundleMessage,
      messageText: String,
      ignoreMessagePrefix: Option[String]
  ): Boolean =
    (ignoreMessagePrefix, replyMessageBundle.matcher, replyMessageBundle.trigger) match {
      case (Some(prefix), _, _) if messageText.startsWith(prefix) => false
      case (_, ContainsOnce, TextTrigger(triggers @ _*))
          if (triggers.exists(TextTriggerValue.matchValue(_, messageText.toLowerCase()))) =>
        true
      case (_, ContainsAll, TextTrigger(triggers @ _*))
          if (triggers.forall(TextTriggerValue.matchValue(_, messageText.toLowerCase()))) =>
        true
      case (_, _, MessageLengthTrigger(messageLength)) if messageText.size >= messageLength => true
      case _                                                                                => false
    }
}
