package root.infrastructure

import info.mukel.telegrambot4s._, api._, methods._, models._, declarative._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait MediaFiles

case class Mp3Files(files : List[String]) extends MediaFiles
case class GifFiles(files : List[String]) extends MediaFiles
case class MultimediaFiles(
  mp3Files : List[String],
  gifFiles : List[String]
) extends MediaFiles

object MediaFiles {

  type MediaAction[T <: MediaFiles] =
    T => Message => Future[List[Message]]

  def toMessageReply(mf : MediaFiles)(
    implicit audioAction : MediaAction[Mp3Files],
    gifAction : MediaAction[GifFiles]
  ) : Message => Future[List[Message]] =
    (m : Message) => mf match {
      case Mp3Files(x) => audioAction(Mp3Files(x))(m)
      case GifFiles(x) => gifAction(GifFiles(x))(m)
      case MultimediaFiles(mp3s, gifs) => for {
        lm1 <- audioAction(Mp3Files(mp3s))(m)
        lm2 <- gifAction(GifFiles(gifs))(m)
      } yield lm1 ++ lm2
    }
}

case class ReplyBundle(
  triggers: List[String],
  files: MediaFiles,
  matcher: MessageMatches = ContainsOnce
)

case class ReplyBundleRefined(
  triggers: List[String],
  messageReply: Message => Future[List[Message]],
  matcher: MessageMatches = ContainsOnce
)
