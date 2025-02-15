package com.benkio.telegrambotinfrastructure.model.media

import cats.syntax.all.*
import cats.Show
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource.given
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import org.http4s.Uri

import java.time.Instant
import scala.util.Try

final case class Media(
    mediaName: String,
    kinds: List[String],
    mimeType: String,
    mediaSources: List[Either[String, Uri]],
    mediaCount: Int,
    createdAt: Instant
)

object Media {

  def apply(dbMediaData: DBMediaData): Either[Throwable, Media] = for {
    createdAt <- Try(Instant.ofEpochSecond(dbMediaData.created_at.toLong)).toEither
  } yield Media(
    mediaName = dbMediaData.media_name,
    kinds = decode[List[String]](dbMediaData.kinds)
      .handleErrorWith(_ => decode[String](dbMediaData.kinds).flatMap(decode[List[String]]))
      .getOrElse(List.empty),
    mimeType = dbMediaData.mime_type,
    mediaSources = decode[List[Either[String, Uri]]](dbMediaData.media_sources)
      .handleErrorWith(_ => decode[String](dbMediaData.media_sources).flatMap(decode[List[Either[String, Uri]]]))
      .getOrElse(List.empty),
    mediaCount = dbMediaData.media_count,
    createdAt = createdAt
  )

  given mediaShowInstance: Show[Media] =
    Show.show(media =>
      s"${media.mediaCount.toString.padTo(4, ' ')} | ${media.mediaName} | ${media.mediaSources.asJson.noSpaces}"
    )

  def mediaListToString(medias: List[Media]): String =
    medias.map(_.show).mkString("\n")
}
