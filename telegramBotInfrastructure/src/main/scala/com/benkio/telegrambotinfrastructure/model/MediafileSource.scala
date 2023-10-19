package com.benkio.telegrambotinfrastructure.model

import cats.implicits._
import io.circe.generic.semiauto._
import io.circe.Decoder
import io.circe.HCursor
import io.circe.DecodingFailure

import java.net.URL
import scala.util.Try

final case class MediafileSource(
    filename: String,
    kind: Option[String],
    mime: Option[String],
    url: URL
)

object MediafileSource {

  given Decoder[MediafileSource] = deriveDecoder
  given Decoder[URL] = new Decoder[URL] {
    final def apply(c: HCursor): Decoder.Result[URL] =
      for {
        urlString <- c.as[String]
        url <- Try(new URL(urlString)).toEither.leftMap(_ =>
          DecodingFailure(s"Couldn't parse the URL: $urlString", List.empty)
        )
      } yield url
  }
}
