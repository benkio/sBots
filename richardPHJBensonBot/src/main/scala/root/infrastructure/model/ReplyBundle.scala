package root.infrastructure.model

import root.infrastructure.{ContainsOnce, MessageMatches}

final case class ReplyBundle(
  triggers: List[String],
  mediafiles: List[MediaFile] = List.empty[MediaFile],
  text : List[Text] = List.empty[Text],
  replyMessageId : Option[Int] = None,
  isCommand : Boolean = false,
  matcher: MessageMatches = ContainsOnce
)
