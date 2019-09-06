package root.infrastructure.model

import info.mukel.telegrambot4s.models.Message
import root.infrastructure.default.Actions.Action

import scala.concurrent.Future

sealed trait Reply

final case class Text(
  text : String
) extends Reply

sealed trait MediaFile extends Reply

final case class Mp3File(filename : String) extends MediaFile {
  require(filename.takeRight(4).equals(".mp3"))
}
final case class GifFile(filename : String) extends MediaFile {
  require(filename.takeRight(4).equals(".gif"))
}
final case class PhotoFile(filename : String) extends MediaFile {
  require(List(".jpg", ".png").contains(filename.takeRight(4)))
}

object MediaFile {

  def toMessageReply(f : MediaFile)(
    implicit audioAction : Action[Mp3File],
    gifAction : Action[GifFile],
    photoAction : Action[PhotoFile],
    m : Message
  ) : Future[Message] = f match {
    case mp3 @ Mp3File(_) => audioAction(mp3)(m)
    case gif @ GifFile(_) => gifAction(gif)(m)
    case photo @ PhotoFile(_) => photoAction(photo)(m)
  }
}
