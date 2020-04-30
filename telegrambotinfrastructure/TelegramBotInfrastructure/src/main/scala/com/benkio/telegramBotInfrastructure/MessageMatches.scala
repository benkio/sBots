package com.benkio.telegramBotInfrastructure

import com.benkio.telegramBotInfrastructure.model.TextTriggerValue
import scala.util.matching.Regex
import com.benkio.telegramBotInfrastructure.model.{
  MessageTrigger,
  ReplyBundleMessage,
  TextTrigger,
  MessageLengthTrigger
}

// Types for managing the matching of words
trait MessageMatches

object ContainsOnce extends MessageMatches
object ContainsAll extends MessageMatches

object MessageMatches {

  def doesMatch(
    replyMessageBundle : ReplyBundleMessage,
    messageText : String,
    ignoreMessagePrefix : Option[String]) : Boolean =
    (ignoreMessagePrefix, replyMessageBundle.matcher, replyMessageBundle.trigger) match {
      case (Some(prefix), _, _) if (messageText.startsWith(prefix)) => false
      case (_, ContainsOnce, TextTrigger(triggers)) if (triggers.exists(TextTriggerValue.matchValue(_, messageText.toLowerCase()))) => true
      case (_, ContainsAll, TextTrigger(triggers)) if (triggers.forall(TextTriggerValue.matchValue(_, messageText.toLowerCase()))) => true
      case (_, _, MessageLengthTrigger(messageLength)) if (messageText.size >= messageLength) => true
      case _ => false
    }
}
