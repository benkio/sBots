package com.benkio.telegramBotInfrastructure

import com.benkio.telegramBotInfrastructure.model.{
  MessageTrigger,
  TextTrigger,
  MessageLengthTrigger
}

// Types for managing the matching of words
trait MessageMatches

object ContainsOnce extends MessageMatches
object ContainsAll extends MessageMatches

object MessageMatches {

  def doesMatch(
    trigger : MessageTrigger,
    messageText : String,
    matcher : MessageMatches = ContainsOnce) : Boolean =
    (matcher, trigger) match {
      case (ContainsOnce, TextTrigger(triggers)) if (triggers.exists(k => messageText.toLowerCase() contains k)) => true
      case (ContainsAll, TextTrigger(triggers)) if (triggers.forall(k => messageText.toLowerCase() contains k)) => true
      case (_, MessageLengthTrigger(messageLength)) if (messageText.size >= messageLength) => true
      case _ => false
    }
}
