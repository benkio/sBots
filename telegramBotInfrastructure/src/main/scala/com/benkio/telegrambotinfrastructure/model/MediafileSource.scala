package com.benkio.telegrambotinfrastructure.model

import cats.implicits.*
import io.circe.generic.semiauto.*
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

  given Decoder[MediaFileSource] = deriveDecoder
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
