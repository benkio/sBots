package com.benkio.telegrambotinfrastructure.resources.db

import cats.effect.Async
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.model.show.RandomQuery
import com.benkio.telegrambotinfrastructure.model.show.Show
import com.benkio.telegrambotinfrastructure.model.show.ShowQuery
import com.benkio.telegrambotinfrastructure.model.show.ShowQueryKeyword
import doobie.*
import doobie.implicits.*
import log.effect.LogWriter
import io.circe.*
import io.circe.generic.semiauto.*

import java.time.format.DateTimeFormatter

final case class DBShowData(
    show_url: String,
    bot_name: String,
    show_title: String,
    show_upload_date: String,
    show_duration: Int,
    show_description: Option[String],
    show_is_live: Boolean,
    show_origin_automatic_caption: Option[String]
)

object DBShowData {

  given Decoder[DBShowData]                = deriveDecoder[DBShowData]
  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

  def apply(show: Show): DBShowData = DBShowData(
    show_url = show.url.renderString,
    bot_name = show.botName,
    show_title = show.title,
    show_upload_date = show.uploadDate.format(dateTimeFormatter),
    show_duration = show.duration,
    show_description = show.description,
    show_is_live = show.isLive,
    show_origin_automatic_caption = show.originAutomaticCaption
  )
}

trait DBShow[F[_]] {
  def getShows(botName: String): F[List[DBShowData]]
  def getShowByShowQuery(query: ShowQuery, botName: String): F[List[DBShowData]]
  def insertShow(dbShowData: DBShowData): F[Unit]
}

object DBShow {

  def apply[F[_]: Async](
      transactor: Transactor[F],
  )(using log: LogWriter[F]): DBShow[F] =
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

    override def insertShow(dbShowData: DBShowData): F[Unit] =
      insertShowQuery(dbShowData).run.transact(transactor).void.exceptSql {
        case e if e.getMessage().contains("UNIQUE constraint failed") =>
          updateOnConflictSql(dbShowData).run.transact(transactor).void
        case e =>
          Async[F].raiseError(
            new RuntimeException(s"An error occurred in inserting $dbShowData with exception: $e")
          )
      }
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
    sql"SELECT show_url, bot_name, show_title, show_upload_date, show_duration, show_description, show_is_live, show_origin_automatic_caption FROM show WHERE bot_name = $botName"
      .query[DBShowData]
  def getShowByShowQueryQuery(query: ShowQuery, botName: String): Query0[DBShowData] = {
    val q =
      fr"SELECT show_url, bot_name, show_title, show_upload_date, show_duration, show_description, show_is_live, show_origin_automatic_caption FROM show" ++
        Fragments.whereAnd(
          fr"bot_name = $botName",
          showQueryToFragments(query)*
        )

    q.query[DBShowData]
  }
  def insertShowQuery(dbShowData: DBShowData): Update0 =
    sql"INSERT INTO show (show_url, bot_name, show_title, show_upload_date, show_duration, show_description, show_is_live, show_origin_automatic_caption) VALUES (${dbShowData.show_url}, ${dbShowData.bot_name}, ${dbShowData.show_title}, ${dbShowData.show_upload_date}, ${dbShowData.show_duration}, ${dbShowData.show_description}, ${dbShowData.show_is_live}, ${dbShowData.show_origin_automatic_caption})".update

  def updateOnConflictSql(dbShowData: DBShowData): Update0 =
    sql"UPDATE show SET bot_name = ${dbShowData.bot_name}, show_title = ${dbShowData.show_title}, show_upload_date = ${dbShowData.show_upload_date}, show_duration = ${dbShowData.show_duration}, show_description = ${dbShowData.show_description}, show_is_live = ${dbShowData.show_is_live}, show_origin_automatic_caption = ${dbShowData.show_origin_automatic_caption} WHERE show_url = ${dbShowData.show_url};".update
}
