package com.benkio.telegrambotinfrastructure.model.show

import cats.implicits.*
import cats.MonadThrow
import cats.{ Show => CatsShow }
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import org.http4s.Uri

import java.time.LocalDate
import scala.util.Try

final case class Show(
    url: Uri,
    botName: String,
    title: String,
    uploadDate: LocalDate,
    duration: Int,
    description: Option[String]
)

object Show {
  def apply[F[_]: MonadThrow](dbShow: DBShowData): F[Show] = for {
    url <- MonadThrow[F].fromEither(Uri.fromString(dbShow.show_url))
    uploadDate <- MonadThrow[F].fromEither(
      Try(
        LocalDate.parse(dbShow.show_upload_date, DBShowData.dateTimeFormatter)
      ).toEither
    )
  } yield Show(
    url = url,
    botName = dbShow.bot_name,
    title = dbShow.show_title,
    uploadDate = uploadDate,
    duration = dbShow.show_duration,
    description = dbShow.show_description
  )

  given showInstance: CatsShow[Show] =
    CatsShow.show(show =>
      s"""${show.uploadDate} - ${show.url}
 ${show.title}""" +
        show.description.fold("")(d => s"""\n----------\n $d""")
    )
}
