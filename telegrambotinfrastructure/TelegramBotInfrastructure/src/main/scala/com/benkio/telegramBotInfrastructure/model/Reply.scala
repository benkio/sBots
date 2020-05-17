package com.benkio.telegramBotInfrastructure.model

import info.mukel.telegrambot4s.models.Message
import com.benkio.telegramBotInfrastructure.default.Actions.Action
import scala.concurrent.Future

sealed trait Reply

final case class TextReply(
    text: Message => List[String],
    replyToMessage: Boolean = false
) extends Reply

sealed trait MediaFile extends Reply {
  def filename: String
}

final case class Mp3File private[model] (filename: String) extends MediaFile {
  require(filename.endsWith(".mp3"))
}

final case class GifFile private[model] (filename: String) extends MediaFile {
  require(filename.endsWith(".gif"))
}

final case class PhotoFile private[model] (filename: String) extends MediaFile {
  require(List(".jpg", ".png").exists(filename.endsWith(_)))
}

object MediaFile {

  def apply(filename: String): Option[MediaFile] = filename match {
    case s if s.endsWith(".mp3")                         => Some(Mp3File(s))
    case s if s.endsWith(".gif")                         => Some(GifFile(s))
    case s if List(".jpg", ".png").exists(s.endsWith(_)) => Some(PhotoFile(s))
    case _                                               => None
  }
}

object Reply {

  def toMessageReply(f: Reply, m: Message)(
      implicit audioAction: Action[Mp3File],
      gifAction: Action[GifFile],
      photoAction: Action[PhotoFile],
      textAction: Action[TextReply]
  ): Future[Message] = f match {
    case mp3 @ Mp3File(_)       => audioAction(mp3)(m)
    case gif @ GifFile(_)       => gifAction(gif)(m)
    case photo @ PhotoFile(_)   => photoAction(photo)(m)
    case text @ TextReply(_, _) => textAction(text)(m)
  }

}
