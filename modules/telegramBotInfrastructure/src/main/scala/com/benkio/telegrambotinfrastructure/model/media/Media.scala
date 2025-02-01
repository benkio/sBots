package com.benkio.telegrambotinfrastructure.model.media

import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import cats.Show
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import org.http4s.Uri
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource.given

import java.time.Instant
import scala.util.Try

final case class Media(
    mediaName: String,
    kinds: List[String],
    mediaSources: List[Either[String, Uri]],
    mediaCount: Int,
    createdAt: Instant
)

object Media {

  def apply(dbMediaData: DBMediaData): Either[Throwable, Media] = for {
    createdAt <- Try(Instant.ofEpochSecond(dbMediaData.created_at.toLong)).toEither
  } yield Media(
    mediaName = dbMediaData.media_name,
    kinds = decode[List[String]](dbMediaData.kinds.getOrElse("[]")).getOrElse(List.empty),
    mediaSources = decode[List[Either[String,Uri]]](dbMediaData.media_sources).getOrElse(List.empty),
    mediaCount = dbMediaData.media_count,
    createdAt = createdAt,
  )

  given mediaShowInstance: Show[Media] =
    Show.show(media => s"${media.mediaCount.toString.padTo(4, ' ')} | ${media.mediaName} | ${media.mediaSources.map(_.fold(identity, _.toString)).asJson.toString}")

  def mediaListToString(medias: List[Media]): String =
    medias.map(_.show).mkString("\n")
}
