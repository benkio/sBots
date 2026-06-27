package com.benkio.chatcore.model.show

import cats.implicits.*
import cats.MonadThrow
import cats.Show as CatsShow
import com.benkio.chatcore.conversions.JsonConversions.SrtDecoder.given
import com.benkio.chatcore.conversions.YouTubeTimestamp.finiteDurationToYoutubeTimestamp
import com.benkio.chatcore.model.show.ShowQuery.searchTimestamp
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBShowData
import io.circe.parser.decode

import java.time.LocalDate
import scala.collection.immutable.SortedMap
import scala.concurrent.duration.FiniteDuration
import scala.util.Try

final case class Show(
    id: String,
    botId: SBotId,
    title: String,
    uploadDate: LocalDate,
    duration: Int,
    description: Option[String],
    isLive: Boolean,
    originAutomaticCaption: Option[String],
    originAutomaticCaptionSrt: SortedMap[FiniteDuration, String],
    timestamp: Option[FiniteDuration]
)

extension (show: Show) {
  def addTimestamp(query: ShowQuery): Show =
    show.copy(timestamp = searchTimestamp(show.originAutomaticCaptionSrt, query))
}

object Show {
  private given Ordering[FiniteDuration] = Ordering.by(_.toMillis)

  def apply[F[_]: MonadThrow](dbShow: DBShowData): F[Show] = for {
    uploadDate <- MonadThrow[F].fromEither(
      Try(
        LocalDate.parse(dbShow.show_upload_date, DBShowData.dateTimeFormatter)
      ).toEither
    )
    originAutomaticCaptionSrt <- MonadThrow[F].fromEither {
      if dbShow.show_origin_automatic_caption_srt.trim.isEmpty
      then Right(SortedMap.empty[FiniteDuration, String])
      else
        decode[Map[FiniteDuration, String]](dbShow.show_origin_automatic_caption_srt).map(decodedCaptionMap =>
          SortedMap.from(decodedCaptionMap)
        )
    }
  } yield Show(
    id = dbShow.show_id,
    botId = SBotId(dbShow.bot_id),
    title = dbShow.show_title,
    uploadDate = uploadDate,
    duration = dbShow.show_duration,
    description = dbShow.show_description,
    isLive = dbShow.show_is_live,
    originAutomaticCaption = dbShow.show_origin_automatic_caption,
    originAutomaticCaptionSrt = originAutomaticCaptionSrt,
    timestamp = None
  )

  given showInstance: CatsShow[Show] =
    CatsShow.show(show => {
      val description    = show.description.getOrElse("")
      val timestampQuery = show.timestamp
        .map(timestamp => s"&t=${finiteDurationToYoutubeTimestamp(timestamp)}")
        .getOrElse("")
      s"""${show.uploadDate} - https://www.youtube.com/watch?v=${show.id}$timestampQuery
         | ${show.title}
         |----------
         | $description""".stripMargin
    })
}
