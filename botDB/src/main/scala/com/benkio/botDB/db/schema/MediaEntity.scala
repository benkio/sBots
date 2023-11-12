package com.benkio.botDB.db.schema

import cats.ApplicativeThrow

import java.net.URL
import java.sql.Timestamp

final case class MediaEntity(
    media_name: String,
    kinds: List[String],
    mime_type: String,
    media_url: URL,
    created_at: Timestamp
)

object MediaEntity {

  final case class MediaNameExtensionNotRecognized(media_name: String)
      extends Throwable(s"Media name extension not recognized: $media_name")

  def mimeTypeOrDefault[F[_]: ApplicativeThrow](media_name: String, mime_type: Option[String]): F[String] =
    mime_type.fold[F[String]](media_name.takeRight(3) match {
      case "gif" => ApplicativeThrow[F].pure("image/gif")
      case "jpg" => ApplicativeThrow[F].pure("image/jpeg")
      case "png" => ApplicativeThrow[F].pure("image/png")
      case "mp3" => ApplicativeThrow[F].pure("audio/mpeg")
      case "mp4" => ApplicativeThrow[F].pure("video/mp4")
      case _     => ApplicativeThrow[F].raiseError(MediaNameExtensionNotRecognized(media_name))
    })(ApplicativeThrow[F].pure)

}
