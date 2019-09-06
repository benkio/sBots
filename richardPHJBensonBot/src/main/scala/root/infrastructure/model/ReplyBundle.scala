package root.infrastructure.model

import root.infrastructure.{ContainsOnce, MessageMatches}

final case class ReplyBundle(
  triggers: List[String],
  mp3files: List[Mp3File] = List.empty[Mp3File],
  giffiles: List[GifFile] = List.empty[GifFile],
  photofiles: List[PhotoFile] = List.empty[PhotoFile],
  text : List[Text] = List.empty[Text],
  replyMessageId : Option[Int] = None,
  isCommand : Boolean = false,
  matcher: MessageMatches = ContainsOnce
)
