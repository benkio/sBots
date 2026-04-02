package com.benkio.chatcore.model.media

import cats.syntax.all.*
import com.benkio.chatcore.conversions.Json.decodeStringToJson
import com.benkio.chatcore.model.media.MediaFileSource.given
import com.benkio.chatcore.model.MimeType
import com.benkio.chatcore.model.MimeTypeOps
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBMediaData
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
    kinds = decodeStringToJson[String](dbMediaData.kinds),
    mimeType = MimeTypeOps.mimeTypeOrDefault(dbMediaData.media_name, dbMediaData.mime_type.some),
    mediaSources = decodeStringToJson[Either[String, Uri]](dbMediaData.media_sources),
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
