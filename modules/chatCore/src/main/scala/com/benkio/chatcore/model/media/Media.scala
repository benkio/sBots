package com.benkio.chatcore.model.media

import cats.syntax.all.*
import com.benkio.chatcore.conversions.JsonConversions.decodeStringToJson
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
    mediaSources: List[MediaSource],
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
    mediaSources = decodeSources(dbMediaData.media_sources),
    mediaCount = dbMediaData.media_count,
    createdAt = createdAt
  )

  /** The stored sources. An entry that is neither an url nor a tagged reference is unusable: it is left out, so it does
    * not take the valid ones down with it.
    */
  private def decodeSources(stored: String): List[MediaSource] =
    decodeStringToJson[String](stored).flatMap(MediaSource.parse)

  extension (media: Media) {
    def getLink: Option[Uri] = media.mediaSources.collectFirst { case MediaSource.Url(uri) => uri }
    def toHtmlRow: String    =
      s"""| ${media.mediaCount.toString
          .padTo(5, ' ')} | <a href="${media.getLink.map(_.toString).getOrElse("")}">${media.mediaName}</a> |"""
  }
}
