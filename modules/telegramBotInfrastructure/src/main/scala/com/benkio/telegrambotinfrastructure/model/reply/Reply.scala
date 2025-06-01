package com.benkio.telegrambotinfrastructure.model.reply

import cats.effect.SyncIO
import cats.syntax.all.*
import cats.Applicative
import cats.ApplicativeThrow
import com.benkio.telegrambotinfrastructure.model.reply.toText
import io.circe.*
import io.circe.generic.semiauto.*
import telegramium.bots.Message

sealed trait Reply[F[_]] {
  val replyToMessage: Boolean
}

final case class TextReplyM[F[_]](
    textM: Message => F[List[Text]],
    replyToMessage: Boolean = false
) extends Reply[F]

final case class TextReply[F[_]](
    text: List[Text],
    replyToMessage: Boolean = false
) extends Reply[F]

object TextReply:
  def fromList[F[_]](values: String*)(
      replyToMessage: Boolean
  ): TextReply[F] =
    TextReply(
      text = values.toList.toText,
      replyToMessage = replyToMessage
    )

final case class MediaReply[F[_]](
    mediaFiles: F[List[MediaFile]],
    replyToMessage: Boolean = false
) extends Reply[F]

object MediaReply:
  def fromList[F[_]: Applicative](mediaFiles: List[MediaFile]): MediaReply[F] = MediaReply[F](
    mediaFiles = mediaFiles.pure[F]
  )

object Reply:

  given replyDecoder[F[_]: Applicative]: Decoder[Reply[F]] = deriveDecoder[Reply[F]]
  given replyEncoder: Encoder[Reply[SyncIO]]               = deriveEncoder[Reply[SyncIO]]

  given mediaFileListDecoder[F[_]: Applicative]: Decoder[F[List[MediaFile]]] =
    Decoder[List[MediaFile]]
      .map(Applicative[F].pure)
  given mediaFileListEncoder: Encoder[SyncIO[List[MediaFile]]] =
    Encoder[List[MediaFile]]
      .contramap(_.unsafeRunSync())

  // We can't translate to json such functions. This should never
  // being called and the `TextReplyM` should remain in scala code only
  given failingDecoder[F[_]]: Decoder[Message => F[List[Text]]] =
    Decoder.failed[Message => F[List[Text]]](
      DecodingFailure("Can't decode fuction `Message => F[List[Text]]`", List.empty)
    )
  given failingEncoder[F[_]]: Encoder[Message => F[List[Text]]] =
    Encoder.instance[Message => F[List[Text]]](_ => Json.Null)

  extension [F[_]: ApplicativeThrow](r: Reply[F])
    def prettyPrint: F[List[String]] = r match {
      case TextReply(txt, _) => ApplicativeThrow[F].pure(txt.map(_.show))
      case TextReplyM(_, _)  =>
        ApplicativeThrow[F].raiseError(Throwable("[Reply] Can't carr `prettyPrint` on a `TextReplyM`"))
      case MediaReply(mediaFilesF, _) => mediaFilesF.map(mfs => mfs.map(_.show))
    }
