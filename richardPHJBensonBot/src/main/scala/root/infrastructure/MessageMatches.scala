package root.infrastructure

import info.mukel.telegrambot4s._, models._

import scala.concurrent.Future

// Types for managing the matching of words
trait MessageMatches

object ContainsOnce extends MessageMatches
object ContainsAll extends MessageMatches

object MessageMatches {

  def doesMatch(
    triggers : List[String],
    messageText : String,
    messageReply: Future[List[Message]],
    matcher : MessageMatches = ContainsOnce) : Option[Future[List[Message]]] =
    matcher match {
      case ContainsOnce if (triggers.exists(k => messageText.toLowerCase() contains k)) => Some(messageReply)
      case ContainsAll if (triggers.forall(k => messageText.toLowerCase() contains k)) => Some(messageReply)
      case _ => None
    }
}
