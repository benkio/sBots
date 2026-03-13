package com.benkio.chatcore.model.media

import cats.syntax.all.*
import com.benkio.chatcore.model.media.MediaFileSource.given
import com.benkio.chatcore.model.MimeType
import com.benkio.chatcore.model.MimeTypeOps
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBMediaData
import io.circe.parser.decode
import org.http4s.Uri

import java.time.Instant
import scala.util.Try

final case class Media(
    mediaName: String,
    botId: SBotId,
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
    botId = SBotId(dbMediaData.bot_id),
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

  extension (media: Media) {
    def getLink: Option[Uri] = media.mediaSources.collectFirst { case Right(uri) => uri }
    def toHtmlRow: String    =
      s"""| ${media.mediaCount.toString
          .padTo(5, ' ')} | <a href="${media.getLink.map(_.toString).getOrElse("")}">${media.mediaName}</a> |"""
  }
}
