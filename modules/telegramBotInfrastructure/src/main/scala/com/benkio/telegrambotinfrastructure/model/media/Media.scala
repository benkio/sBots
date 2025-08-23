package com.benkio.telegrambotinfrastructure.model.media

import cats.syntax.all.*
import cats.Show
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource.given
import com.benkio.telegrambotinfrastructure.model.MimeType
import com.benkio.telegrambotinfrastructure.model.MimeTypeOps
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import io.circe.parser.decode
import org.http4s.Uri

import java.time.Instant
import scala.util.Try

final case class Media(
    mediaName: String,
    kinds: List[String],
    mimeType: MimeType,
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
    mimeType = MimeTypeOps.mimeTypeOrDefault(dbMediaData.media_name, dbMediaData.mime_type.some),
    mediaSources = decode[List[Either[String, Uri]]](dbMediaData.media_sources)
      .handleErrorWith(_ => decode[String](dbMediaData.media_sources).flatMap(decode[List[Either[String, Uri]]]))
      .getOrElse(List.empty),
    mediaCount = dbMediaData.media_count,
    createdAt = createdAt
  )

  extension (media: Media) def getLink: Option[Uri] = media.mediaSources.collectFirst { case Right(uri) => uri }

  given mediaShowInstance: Show[Media] =
    Show.show(media =>
      s"""| ${media.mediaCount.toString
          .padTo(5, ' ')} | [${media.mediaName}](${media.getLink.map(_.toString).getOrElse("")})"""
    )

  def mediaListToMarkdown(medias: List[Media]): String =
    if medias.length == 0 then {
      ""
    } else {
      val contentShow = medias.map(_.show)
      val contentMax  = contentShow.maxBy(_.length).length
      val padding     = contentMax - 15
      val content     = contentShow.map(_.padTo(contentMax, ' ') + " |")
      println(s"[Media] test: ${content}")
      s"""```
| Count | File ${" " * padding} |
| ----- | -----${"-" * padding} |
${content.mkString("\n")}
```"""
    }

}
