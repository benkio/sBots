package com.benkio.telegrambotinfrastructure.model

import java.time.Instant
import cats.Show
import cats.syntax.all._
import org.http4s.Uri

final case class Media(
    mediaName: String,
    kind: Option[String],
    mediaUrl: Uri,
    mediaCount: Int,
    created_at: Instant
)

object Media {

  implicit val mediaShowInstance: Show[Media] =
    Show.show(media => s"${media.mediaCount.toString.padTo(4, ' ')} | ${media.mediaName} | ${media.mediaUrl}")

  def mediaListToString(medias: List[Media]): String =
    medias.map(_.show).mkString("\n")
}
