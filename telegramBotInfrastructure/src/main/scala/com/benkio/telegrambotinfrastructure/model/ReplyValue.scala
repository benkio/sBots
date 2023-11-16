package com.benkio.telegrambotinfrastructure.model

import cats.Show



sealed trait ReplyValue

final case class Text(value: String) extends ReplyValue

object Text:
    given Show[Text] = Show.show(_.value)
    extension (values: List[String])
      def toText: List[Text] = values.map(Text(_))

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

object MediaFile {

  implicit val showInstance: Show[MediaFile] = Show.show(_.filename)

  def apply(filepath: String, replyToMessage: Boolean = false): MediaFile = filepath match {
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
