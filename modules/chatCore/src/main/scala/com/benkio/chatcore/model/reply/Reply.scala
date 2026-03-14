package com.benkio.chatcore.model.reply

import cats.syntax.all.*
import io.circe.generic.semiauto.*
import io.circe.Decoder
import io.circe.Encoder

sealed trait Reply {
  val replyToMessage: Boolean
}

final case class TextReply(
    text: List[Text],
    replyToMessage: Boolean = false
) extends Reply

object TextReply {
  def fromList(values: String*)(
      replyToMessage: Boolean
  ): TextReply =
    TextReply(
      text = values.toList.toText,
      replyToMessage = replyToMessage
    )
}

final case class MediaReply(
    mediaFiles: List[MediaFile],
    replyToMessage: Boolean = false
) extends Reply

object MediaReply {
  def fromList(mediaFiles: List[MediaFile]): MediaReply = MediaReply(
    mediaFiles = mediaFiles
  )
}

// Holds a key that will later be resolved to an effectful computation
final case class EffectfulReply(key: EffectfulKey, replyToMessage: Boolean = false) extends Reply

object Reply {

  given replyDecoder: Decoder[Reply] = deriveDecoder[Reply]
  given replyEncoder: Encoder[Reply] = deriveEncoder[Reply]

  extension (r: Reply) {
    def prettyPrint: List[String] = r match {
      case TextReply(txt, _)         => txt.map(_.show)
      case EffectfulReply(key, _)    => List(s"Reply for `$key`")
      case MediaReply(mediaFiles, _) => mediaFiles.map(_.show)
    }
    def replyValues: List[ReplyValue] = r match {
      case TextReply(txt, _)         => txt
      case EffectfulReply(_, _)      => List.empty
      case MediaReply(mediaFiles, _) => mediaFiles
    }
  }
}
