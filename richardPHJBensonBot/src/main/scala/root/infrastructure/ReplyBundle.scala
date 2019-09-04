package root.infrastructure

import info.mukel.telegrambot4s._, api._, methods._, models._, declarative._

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
    case mp3 @ Mp3File(x) => audioAction(mp3)(m)
    case gif @ GifFile(x) => gifAction(gif)(m)
  }
}

case class ReplyBundle(
  triggers: List[String],
  mp3files: List[Mp3File] = List.empty[Mp3File],
  giffiles: List[GifFile] = List.empty[GifFile],
  matcher: MessageMatches = ContainsOnce
)

case class ReplyBundleRefined(
  triggers: List[String],
  messageReply: Future[List[Message]],
  matcher: MessageMatches = ContainsOnce
)(implicit message : Message)

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
