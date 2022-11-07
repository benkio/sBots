package com.benkio.telegrambotinfrastructure.model

import cats.Show
import cats.syntax.all._
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import org.http4s.Uri

import java.time.Instant
import scala.util.Try

final case class Media(
    mediaName: String,
    kind: Option[String],
    mediaUrl: Uri,
    mediaCount: Int,
    createdAt: Instant
)

object Media {

  def apply(dbMediaData: DBMediaData): Either[Throwable, Media] = for {
    uri       <- Uri.fromString(dbMediaData.media_url)
    createdAt <- Try(Instant.parse(dbMediaData.created_at)).toEither
  } yield Media(
    mediaName = dbMediaData.media_name,
    kind = dbMediaData.kind,
    mediaUrl = uri,
    mediaCount = dbMediaData.media_count,
    createdAt = createdAt,
  )

  implicit val mediaShowInstance: Show[Media] =
    Show.show(media => s"${media.mediaCount.toString.padTo(4, ' ')} | ${media.mediaName} | ${media.mediaUrl}")

  def mediaListToString(medias: List[Media]): String =
    medias.map(_.show).mkString("\n")
}
