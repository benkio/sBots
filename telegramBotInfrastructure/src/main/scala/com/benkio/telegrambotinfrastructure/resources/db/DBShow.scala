package com.benkio.telegrambotinfrastructure.resources.db

import cats.effect.Async
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.RandomQuery
import com.benkio.telegrambotinfrastructure.model.Show
import com.benkio.telegrambotinfrastructure.model.ShowQuery
import com.benkio.telegrambotinfrastructure.model.ShowQueryKeyword
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
  def getShowByShowQuery(query: ShowQuery, botName: String): F[List[DBShowData]]
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
      DBShow.getShowsQuery(botName).stream.compile.toList.transact(transactor) <* log.info(
        s"get shows by bot name: $botName"
      )
    override def getShowByShowQuery(query: ShowQuery, botName: String): F[List[DBShowData]] =
      DBShow.getShowByShowQueryQuery(query, botName).stream.compile.toList.transact(transactor) <* log.info(
        s"get shows by bot name: $botName and keyword: $query"
      )
  }

  private def showQueryToFragments(query: ShowQuery): List[Fragment] = query match {
    case RandomQuery => List.empty
    case ShowQueryKeyword(titleKeywords, descriptionKeywords, minDuration, maxDuration, minDate, maxDate) =>
      titleKeywords.toList.flatten.map(k => fr"""lower(show_title) LIKE ${"%" + k + "%"}""") ++
        descriptionKeywords.toList.flatten.map(k => fr"""lower(show_description) LIKE ${"%" + k + "%"}""") ++
        minDuration.toList.map(mind => fr"show_duration > $mind") ++
        maxDuration.toList.map(maxd => fr"show_duration < $maxd") ++
        minDate.toList.map(mind => fr"show_upload_date > ${mind.format(DBShowData.dateTimeFormatter)}") ++
        maxDate.toList.map(maxd => fr"show_upload_date < ${maxd.format(DBShowData.dateTimeFormatter)}")
  }

  def getShowsQuery(botName: String): Query0[DBShowData] =
    sql"SELECT show_url, bot_name, show_title, show_upload_date, show_duration, show_description FROM show WHERE bot_name = $botName"
      .query[DBShowData]
  def getShowByShowQueryQuery(query: ShowQuery, botName: String): Query0[DBShowData] = {
    val q = fr"SELECT show_url, bot_name, show_title, show_upload_date, show_duration, show_description FROM show" ++
      Fragments.whereAnd(
        fr"bot_name = $botName",
        showQueryToFragments(query): _*
      )
    println(s"query: $q")
    q.query[DBShowData]
  }

}
