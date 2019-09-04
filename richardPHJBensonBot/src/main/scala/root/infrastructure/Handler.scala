package root.infrastructure

import info.mukel.telegrambot4s._, api._, methods._, models._, declarative._

import scala.concurrent.Future

// Types for managing the matching of words
trait MessageMatches

object ContainsOnce extends MessageMatches
object ContainsAll extends MessageMatches

object MessageMatches {

  def getHandler(keys : List[String],
    messageText : String,
    messageReply: Future[List[Message]],
    matcher : MessageMatches = ContainsOnce) : Option[Future[List[Message]]] =
    matcher match {
      case ContainsOnce if (keys.exists(k => messageText.toLowerCase() contains k)) => Some(messageReply)
      case ContainsAll if (keys.forall(k => messageText.toLowerCase() contains k)) => Some(messageReply)
      case _ => None
    }
}
