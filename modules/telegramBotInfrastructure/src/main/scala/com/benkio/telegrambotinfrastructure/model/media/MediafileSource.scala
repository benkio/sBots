package com.benkio.telegrambotinfrastructure.model.media

import cats.implicits.*
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Encoder.encodeString
import io.circe.HCursor
import org.http4s.Uri

final case class MediaFileSource(
    filename: String,
    kinds: List[String],
    mime: String,
    sources: List[Either[String, Uri]]
)

object MediaFileSource {

  def fromString[F[_]: MonadThrow](input: String): F[MediaFileSource] =
    ???
    // for
    //   uri <-  MonadThrow[F].fromEither(Uri.fromString(input))
    // yield MediaFileSource(
    //   filename = filename
    //   ,kinds = List.empty
    //   ,mime = mime
    //   ,sources = uri
    // )

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
            mime = DBMediaData.mimeTypeOrDefault(filename, mime),
            kinds = kinds.getOrElse(List.empty)
          )
        }
    }

}
