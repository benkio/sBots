package com.benkio.telegrambotinfrastructure.model

import cats.Show
import cats.syntax.all._

final case class Media(
    media_name: String,
    kind: Option[String],
    media_url: String,
    media_count: Int,
    created_at: String
)

object Media {

  implicit val mediaShowInstance: Show[Media] =
    Show.show(media => s"${media.media_count.toString.padTo(4, ' ')} | ${media.media_name} | ${media.media_url}")

  def mediaListToString(medias: List[Media]): String =
    medias.map(_.show).mkString("\n")
}
