package com.benkio.telegrambotinfrastructure.resources.db

import cats.effect.Async
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.Show
import doobie._
import doobie.implicits._
import log.effect.LogWriter

import java.time.format.DateTimeFormatter

final case class DBShowData(
    show_url: String,
    bot_name: String,
    show_title: String,
    show_upload_date: String,
    show_duration: Int,
    show_description: Option[String]
)

object DBShowData {

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

  def apply(show: Show): DBShowData = DBShowData(
    show_url = show.url.renderString,
    bot_name = show.botName,
    show_title = show.title,
    show_upload_date = show.uploadDate.format(dateTimeFormatter),
    show_duration = show.duration,
    show_description = show.description
  )
}

trait DBShow[F[_]] {
  def getShows(botName: String): F[List[DBShowData]]
  def getShowByKeywordTitle(keyword: String, botName: String): F[List[DBShowData]]

  def getShowsQuery(botName: String): Query0[DBShowData]
  def getShowByKeywordTitleQuery(keyword: String, botName: String): Query0[DBShowData]
}

object DBShow {

  def apply[F[_]: Async](
      transactor: Transactor[F],
  )(implicit log: LogWriter[F]): DBShow[F] =
    new DBShowImpl[F](
      transactor = transactor,
      log = log
    )

  private[telegrambotinfrastructure] class DBShowImpl[F[_]: Async](
      transactor: Transactor[F],
      log: LogWriter[F]
  ) extends DBShow[F] {

    override def getShows(botName: String): F[List[DBShowData]] =
      getShowsQuery(botName).stream.compile.toList.transact(transactor) <* log.info(
        s"get shows by bot name: $botName"
      )
    override def getShowByKeywordTitle(keyword: String, botName: String): F[List[DBShowData]] =
      getShowByKeywordTitleQuery(keyword, botName).stream.compile.toList.transact(transactor) <* log.info(
        s"get shows by bot name: $botName and keyword: $keyword"
      )

    override def getShowsQuery(botName: String): Query0[DBShowData] =
      sql"SELECT show_url, bot_name, show_title, show_upload_date, show_duration, show_description FROM show WHERE bot_name = $botName"
        .query[DBShowData]
    override def getShowByKeywordTitleQuery(keyword: String, botName: String): Query0[DBShowData] = {
      val q = fr"SELECT show_url, bot_name, show_title, show_upload_date, show_duration, show_description FROM show" ++
        Fragments.whereAnd(
          fr"bot_name = $botName",
          fr"show_title LIKE ${"%" + keyword + "%"}"
        )

      q.query[DBShowData]
    }
  }

}
