package com.benkio.telegrambotinfrastructure.model.media

import cats.implicits.*
import io.circe.Decoder
import io.circe.HCursor

import org.http4s.circe.*

import org.http4s.Uri

final case class MediaFileSource(
    filename: String,
    kinds: Option[List[String]],
    mime: Option[String],
    uri: Uri
)

object MediaFileSource {

  given Decoder[MediaFileSource] =
    new Decoder[MediaFileSource] {
      final def apply(c: HCursor): Decoder.Result[MediaFileSource] =
        for {
          filename <- c.downField("filename").as[String]
          uri      <- c.downField("url").as[Uri]
          mime     <- c.downField("mime").as[Option[String]]
          kinds    <- c.downField("kinds").as[Option[List[String]]]
        } yield {
          MediaFileSource(
            filename = filename,
            uri = uri,
            mime = mime,
            kinds = kinds
          )
        }
    }

}
