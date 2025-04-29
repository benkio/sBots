package com.benkio.telegrambotinfrastructure.model.media

import cats.implicits.*
import cats.MonadThrow
import com.benkio.telegrambotinfrastructure.model.MimeType
import com.benkio.telegrambotinfrastructure.model.MimeTypeOps
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Encoder.encodeString
import io.circe.HCursor
import org.http4s.Uri

final case class MediaFileSource(
    filename: String,
    kinds: List[String],
    mime: MimeType,
    sources: List[Either[String, Uri]]
)

object MediaFileSource {

  def fromUriString[F[_]: MonadThrow](input: String): F[MediaFileSource] =
    for
      uri <- MonadThrow[F].fromEither(Uri.fromString(input))
      filename <- MonadThrow[F].fromOption(
        uri.path.segments.lastOption.map(_.decoded()),
        Throwable(s"[MediafileSource] fromUriString cannot find a filename from this uri $uri")
      )
    yield MediaFileSource(
      filename = filename,
      kinds = List.empty,
      mime = MimeTypeOps.mimeTypeOrDefault(filename, None),
      sources = List(Right(uri))
    )

  given Decoder[Either[String, Uri]] with
    def apply(c: HCursor): Decoder.Result[Either[String, Uri]] =
      c.as[String].map { str =>
        Uri.requestTarget(str).leftMap(_ => str)
      }

  given Encoder[Either[String, Uri]] =
    encodeString.contramap(_.fold(identity, _.toString))

  given Decoder[MediaFileSource] =
    new Decoder[MediaFileSource] {
      final def apply(c: HCursor): Decoder.Result[MediaFileSource] =
        for {
          filename <- c.downField("filename").as[String]
          sources  <- c.downField("sources").as[List[Either[String, Uri]]]
          mime     <- c.downField("mime").as[Option[String]]
          kinds    <- c.downField("kinds").as[Option[List[String]]]
        } yield {
          MediaFileSource(
            filename = filename,
            sources = sources,
            mime = MimeTypeOps.mimeTypeOrDefault(filename, mime),
            kinds = kinds.getOrElse(List.empty)
          )
        }
    }

}
