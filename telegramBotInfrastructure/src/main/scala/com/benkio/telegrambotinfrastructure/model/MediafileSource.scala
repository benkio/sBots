package com.benkio.telegrambotinfrastructure.model

import cats.implicits.*
import io.circe.Decoder
import io.circe.HCursor
import io.circe.DecodingFailure

import java.net.URI
import java.net.URL
import scala.util.Try

final case class MediaFileSource(
    filename: String,
    kinds: Option[List[String]],
    mime: Option[String],
    url: URL
)

object MediaFileSource {

  given Decoder[MediaFileSource] =
    new Decoder[MediaFileSource] {
      final def apply(c: HCursor): Decoder.Result[MediaFileSource] =
        for {
          filename <- c.downField("filename").as[String]
          url      <- c.downField("url").as[URL]
          mime     <- c.downField("mime").as[Option[String]]
          kinds    <- c.downField("kinds").as[Option[List[String]]]
        } yield {
          MediaFileSource(
            filename = filename,
            url = url,
            mime = mime,
            kinds = kinds
          )
        }
    }

  given Decoder[URL] = new Decoder[URL] {
    final def apply(c: HCursor): Decoder.Result[URL] =
      for {
        urlString <- c.as[String]
        url <- Try(URI.create(urlString).toURL()).toEither.leftMap(_ =>
          DecodingFailure(s"Couldn't parse the URL: $urlString", List.empty)
        )
      } yield url
  }
}
