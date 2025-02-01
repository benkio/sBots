package com.benkio.telegrambotinfrastructure.model.media

import cats.implicits.*
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Encoder.encodeString
import io.circe.HCursor
import org.http4s.Uri

final case class MediaFileSource(
    filename: String,
    kinds: Option[List[String]],
    mime: Option[String],
    sources: List[Either[String, Uri]]
)

object MediaFileSource {

  given Decoder[Either[String, Uri]] with
    def apply(c: HCursor): Decoder.Result[Either[String, Uri]] =
      c.as[String].map { str =>
        Uri.fromString(str).leftMap(_ => str)
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
            mime = mime,
            kinds = kinds
          )
        }
    }

}
