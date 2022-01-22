package com.benkio.telegrambotinfrastructure.model

import telegramium.bots.Message

sealed trait Reply {
  val replyToMessage: Boolean
}

final case class TextReply(
    text: Message => List[String],
    replyToMessage: Boolean = false
) extends Reply

sealed trait MediaFile extends Reply {
  def filepath: String
  def filename: String  = filepath.split('/').last
  def extension: String = filename.takeRight(4)
}

final case class Mp3File private[model] (filepath: String, replyToMessage: Boolean = false) extends MediaFile {
  require(filepath.endsWith(".mp3"))
}

final case class GifFile private[model] (filepath: String, replyToMessage: Boolean = false) extends MediaFile {
  require(filepath.endsWith(".gif"))
}

final case class PhotoFile private[model] (filepath: String, replyToMessage: Boolean = false) extends MediaFile {
  require(List(".jpg", ".png").exists(filepath.endsWith(_)))
}

final case class VideoFile private[model] (filepath: String, replyToMessage: Boolean = false) extends MediaFile {
  require(List(".mp4").exists(filepath.endsWith(_)))
}

object MediaFile {

  def apply(filepath: String): MediaFile = filepath match {
    case s if s.endsWith(".mp3")                         => Mp3File(s)
    case s if s.endsWith(".gif")                         => GifFile(s)
    case s if s.endsWith(".mp4")                         => VideoFile(s)
    case s if List(".jpg", ".png").exists(s.endsWith(_)) => PhotoFile(s)
    case _ =>
      throw new IllegalArgumentException(
        s"filepath extension not recognized: $filepath \n allowed extensions: mp3, gif, jpg, png, mp4"
      )
  }
}
