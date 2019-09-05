package root.infrastructure

import info.mukel.telegrambot4s._, models._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait MediaFile

case class Mp3File(filename : String) extends MediaFile
case class GifFile(filename : String) extends MediaFile

object MediaFile {

  type MediaAction[T <: MediaFile] =
    T => Message => Future[Message]

  def toMessageReply(f : MediaFile)(
    implicit audioAction : MediaAction[Mp3File],
    gifAction : MediaAction[GifFile],
    m : Message
  ) : Future[Message] = f match {
    case mp3 @ Mp3File(_) => audioAction(mp3)(m)
    case gif @ GifFile(_) => gifAction(gif)(m)
  }
}

case class ReplyBundle(
  triggers: List[String],
  mp3files: List[Mp3File] = List.empty[Mp3File],
  giffiles: List[GifFile] = List.empty[GifFile],
  text : List[String] = List.empty[String],
  matcher: MessageMatches = ContainsOnce
)

case class ReplyBundleRefined(
  triggers: List[String],
  messageReply: Future[List[Message]],
  matcher: MessageMatches = ContainsOnce
)

object ReplyBundleRefined {

  import MediaFile._

  def refineReplyBundle(replyBundle : ReplyBundle)(
    implicit audioAction : MediaAction[Mp3File],
    gifAction : MediaAction[GifFile],
    message : Message
  ) : ReplyBundleRefined =
    ReplyBundleRefined(
      replyBundle.triggers,
      Future.traverse(
        replyBundle.mp3files ++ replyBundle.giffiles
      )(MediaFile.toMessageReply(_)),
      replyBundle.matcher
    )


}
