package com.benkio.telegrambotinfrastructure.model.reply

import cats.Show
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

object MediaFile {

  given Decoder[MediaFile] = deriveDecoder[MediaFile]
  given Encoder[MediaFile] = deriveEncoder[MediaFile]

  given showInstance: Show[MediaFile] = Show.show(_.filename)

  def fromFilePath(filepath: String, replyToMessage: Boolean = false): MediaFile = filepath match {
    case s if s.endsWith(".mp3")                         => Mp3File(s, replyToMessage)
    case s if s.endsWith(".gif")                         => GifFile(s, replyToMessage)
    case s if s.endsWith(".mp4")                         => VideoFile(s, replyToMessage)
    case s if List(".jpg", ".png").exists(s.endsWith(_)) => PhotoFile(s, replyToMessage)
    case _ =>
      throw new IllegalArgumentException(
        s"filepath extension not recognized: $filepath \n allowed extensions: mp3, gif, jpg, png, mp4"
      )
  }
}

extension (sc: StringContext)
  def mp3(args: Any*): Mp3File   = Mp3File(sc.s(args*))
  def gif(args: Any*): GifFile   = GifFile(sc.s(args*))
  def vid(args: Any*): VideoFile = VideoFile(sc.s(args*))
  def pho(args: Any*): PhotoFile = PhotoFile(sc.s(args*))
  def txt(args: Any*): Text      = Text(sc.s(args*))
  def doc(args: Any*): Document  = Document(sc.s(args*))

extension (values: List[String]) def toText: List[Text] = values.map(Text(_))
