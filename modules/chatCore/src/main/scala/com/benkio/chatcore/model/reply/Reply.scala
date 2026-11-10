package com.benkio.chatcore.model.reply

import cats.syntax.all.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Json

sealed trait Reply {
  val replyToMessage: Boolean
}

/** A reply that is fully described by its data: it needs no effectful computation to become a `ReplyValue`. */
sealed trait PlainReply extends Reply

final case class TextReply(
    text: Set[Text],
    replyToMessage: Boolean = false
) extends PlainReply

object TextReply {
  given Decoder[TextReply] = deriveDecoder[TextReply]
  given Encoder[TextReply] = deriveEncoder[TextReply]

  def fromList(values: String*)(
      replyToMessage: Boolean
  ): TextReply =
    TextReply(
      text = values.toSet.toText,
      replyToMessage = replyToMessage
    )
}

final case class MediaReply(
    mediaFiles: Set[MediaFile],
    replyToMessage: Boolean = false
) extends PlainReply

object MediaReply {
  given Decoder[MediaReply] = deriveDecoder[MediaReply]
  given Encoder[MediaReply] = deriveEncoder[MediaReply]

  def fromList(mediaFiles: List[MediaFile]): MediaReply = MediaReply(
    mediaFiles = mediaFiles.toSet
  )
}

// Holds a key that will later be resolved to an effectful computation
final case class EffectfulReply(key: EffectfulKey, replyToMessage: Boolean = false) extends Reply

object EffectfulReply {
  given Decoder[EffectfulReply] = deriveDecoder[EffectfulReply]
  given Encoder[EffectfulReply] = deriveEncoder[EffectfulReply]
}

// `PlainReply` is a layer of the hierarchy, not of the JSON: both codecs are hand written to keep the flat shape
// `{"TextReply": {...}}`, `{"MediaReply": {...}}`, `{"EffectfulReply": {...}}` the data files are written in.
object PlainReply {

  given plainReplyDecoder: Decoder[PlainReply] = Decoder.instance(c =>
    c.downField("TextReply")
      .as[TextReply]
      .orElse(c.downField("MediaReply").as[MediaReply])
      .map(identity)
  )

  given plainReplyEncoder: Encoder[PlainReply] = Encoder.instance {
    case textReply: TextReply   => Json.obj("TextReply" -> textReply.asJson)
    case mediaReply: MediaReply => Json.obj("MediaReply" -> mediaReply.asJson)
  }
}

object Reply {

  given replyDecoder: Decoder[Reply] = Decoder.instance(c =>
    c.downField("EffectfulReply")
      .as[EffectfulReply]
      .orElse(c.as[PlainReply])
      .map(identity)
  )

  given replyEncoder: Encoder[Reply] = Encoder.instance {
    case plainReply: PlainReply         => plainReply.asJson
    case effectfulReply: EffectfulReply => Json.obj("EffectfulReply" -> effectfulReply.asJson)
  }

  extension (r: Reply) {
    def prettyPrint: List[String] = r match {
      case TextReply(txt, _)         => txt.map(_.show).toList.sorted
      case EffectfulReply(key, _)    => List(s"Reply for `$key`")
      case MediaReply(mediaFiles, _) => mediaFiles.map(_.show).toList.sorted
    }
    def replyValues: Set[ReplyValue] = r match {
      case TextReply(txt, _)         => txt.map(t => t: ReplyValue)
      case EffectfulReply(_, _)      => Set.empty
      case MediaReply(mediaFiles, _) => mediaFiles.map(m => m: ReplyValue)
    }
  }
}
