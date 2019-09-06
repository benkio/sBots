package com.benkio.telegramBotInfrastructure

// Types for managing the matching of words
trait MessageMatches

object ContainsOnce extends MessageMatches
object ContainsAll extends MessageMatches

object MessageMatches {

  def doesMatch(
    triggers : List[String],
    messageText : String,
    matcher : MessageMatches = ContainsOnce) : Boolean =
    matcher match {
      case ContainsOnce if (triggers.exists(k => messageText.toLowerCase() contains k)) => true
      case ContainsAll if (triggers.forall(k => messageText.toLowerCase() contains k)) => true
      case _ => false
    }
}
