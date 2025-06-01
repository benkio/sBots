package com.benkio.telegrambotinfrastructure.model.reply

import cats.syntax.all.*
import cats.MonadThrow
import cats.Show
import com.benkio.telegrambotinfrastructure.model.media.Media
import com.benkio.telegrambotinfrastructure.model.MimeType
import io.circe.*
import io.circe.generic.semiauto.*

sealed trait ReplyValue

object ReplyValue:
  given Decoder[ReplyValue] = deriveDecoder[ReplyValue]
  given Encoder[ReplyValue] = deriveEncoder[ReplyValue]

final case class Text(value: String) extends ReplyValue

object Text:
  given Show[Text]    = Show.show(_.value)
  given Decoder[Text] = deriveDecoder[Text]
  given Encoder[Text] = deriveEncoder[Text]

sealed trait MediaFile extends ReplyValue {
  def filepath: String
  def filename: String  = filepath.split('/').last
  def extension: String = filename.takeRight(4)
}

final case class Mp3File(filepath: String, replyToMessage: Boolean = false) extends MediaFile {
  require(filepath.endsWith(".mp3"))
}

final case class GifFile(filepath: String, replyToMessage: Boolean = false) extends MediaFile {
  require(filepath.endsWith(".gif") || filepath.endsWith(".mp4"))
}

final case class PhotoFile(filepath: String, replyToMessage: Boolean = false) extends MediaFile {
  require(List(".jpg", ".png").exists(filepath.endsWith(_)))
}

final case class VideoFile(filepath: String, replyToMessage: Boolean = false) extends MediaFile {
  require(List(".mp4").exists(filepath.endsWith(_)))
}

final case class Document(filepath: String, replyToMessage: Boolean = false) extends MediaFile {}
final case class Sticker(filepath: String, replyToMessage: Boolean = false)  extends MediaFile {}

object MediaFile {

  given Decoder[MediaFile] = deriveDecoder[MediaFile]
  given Encoder[MediaFile] = deriveEncoder[MediaFile]

  given showInstance: Show[MediaFile] = Show.show(_.filename)

  def fromString[F[_]: MonadThrow](filename: String): F[MediaFile] = filename.takeRight(4) match {
    case ".mp3"                                       => Mp3File(filename).pure[F]
    case ".gif"                                       => GifFile(filename).pure[F]
    case ".jpg" | ".png"                              => PhotoFile(filename).pure[F]
    case ".mp4" if filename.takeRight(7) == "Gif.mp4" => GifFile(filename).pure[F]
    case ".mp4"                                       => VideoFile(filename).pure[F]
    case _                                            =>
      MonadThrow[F].raiseError(
        Throwable(
          s"[ReplyValue] MediaFile.fromString unrecognized filename: $filename. Sticker and Document not supported"
        )
      )
  }

  def fromMimeType(media: Media, replyToMessage: Boolean = false): MediaFile = media.mimeType match {
    case MimeType.GIF     => GifFile(media.mediaName, replyToMessage)
    case MimeType.JPEG    => PhotoFile(media.mediaName, replyToMessage)
    case MimeType.PNG     => PhotoFile(media.mediaName, replyToMessage)
    case MimeType.MPEG    => Mp3File(media.mediaName, replyToMessage)
    case MimeType.STICKER => Sticker(media.mediaName, replyToMessage)
    case MimeType.MP4     => VideoFile(media.mediaName, replyToMessage)
    case MimeType.DOC     => Document(media.mediaName, replyToMessage)
  }
}

extension (sc: StringContext)
  def mp3(args: Any*): Mp3File     = Mp3File(sc.s(args*))
  def gif(args: Any*): GifFile     = GifFile(sc.s(args*))
  def vid(args: Any*): VideoFile   = VideoFile(sc.s(args*))
  def pho(args: Any*): PhotoFile   = PhotoFile(sc.s(args*))
  def txt(args: Any*): Text        = Text(sc.s(args*))
  def doc(args: Any*): Document    = Document(sc.s(args*))
  def sticker(args: Any*): Sticker = Sticker(sc.s(args*))

extension (values: List[String]) def toText: List[Text] = values.map(Text(_))
