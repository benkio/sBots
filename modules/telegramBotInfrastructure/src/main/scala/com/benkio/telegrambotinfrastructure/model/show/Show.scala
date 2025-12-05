package com.benkio.telegrambotinfrastructure.model.show

import cats.implicits.*
import cats.MonadThrow
import cats.Show as CatsShow
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.repository.db.DBShowData

import java.time.LocalDate
import scala.util.Try

final case class Show(
    id: String,
    botId: SBotId,
    title: String,
    uploadDate: LocalDate,
    duration: Int,
    description: Option[String],
    isLive: Boolean,
    originAutomaticCaption: Option[String]
)

object Show {
  def apply[F[_]: MonadThrow](dbShow: DBShowData): F[Show] = for {
    uploadDate <- MonadThrow[F].fromEither(
      Try(
        LocalDate.parse(dbShow.show_upload_date, DBShowData.dateTimeFormatter)
      ).toEither
    )
  } yield Show(
    id = dbShow.show_id,
    botId = SBotId(dbShow.bot_id),
    title = dbShow.show_title,
    uploadDate = uploadDate,
    duration = dbShow.show_duration,
    description = dbShow.show_description,
    isLive = dbShow.show_is_live,
    originAutomaticCaption = dbShow.show_origin_automatic_caption
  )

  given showInstance: CatsShow[Show] =
    CatsShow.show(show =>
      s"""${show.uploadDate} - https://www.youtube.com/watch?v=${show.id}
 ${show.title}""" +
        show.description.fold("")(d => s"""\n----------\n $d""")
    )
}
