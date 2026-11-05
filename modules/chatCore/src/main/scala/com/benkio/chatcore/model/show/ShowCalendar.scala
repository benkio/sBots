package com.benkio.chatcore.model.show

import cats.effect.Sync
import cats.syntax.all.*

import java.time.LocalDate
import java.time.MonthDay
import scala.util.matching.Regex
import scala.util.Try

/** Extracts the date a show is "about" from its title (e.g. `Xah Talk Show 2019-10-18`, `11 GENNAIO 2008`,
  * `25/11/2025`). Pure: the century of two-digit years and the year of year-less dates are derived from the upload
  * year, never from the clock.
  */
object TitleDateParser {

  private val italianMonths: Map[String, Int] = List(
    "gennaio",
    "febbraio",
    "marzo",
    "aprile",
    "maggio",
    "giugno",
    "luglio",
    "agosto",
    "settembre",
    "ottobre",
    "novembre",
    "dicembre"
  ).zipWithIndex.map { case (name, index) => name -> (index + 1) }.toMap

  private val monthAlternatives = italianMonths.keys.mkString("|")

  private val isoRegex: Regex       = """(?<!\d)(\d{4})-(\d{2})-(\d{2})(?!\d)""".r
  private val numericRegex: Regex   = """(?<!\d)(\d{1,2})/(\d{1,2})/(\d{4}|\d{2})(?!\d)""".r
  private val monthNameRegex: Regex =
    s"""(?iu)(?<![\\p{L}\\d])(\\d{1,2})\\s*°?\\s+($monthAlternatives)(?:\\s+(\\d{4})(?!\\d))?(?![\\p{L}])""".r

  private def safeDate(year: Int, month: Int, day: Int): Option[LocalDate] =
    Try(LocalDate.of(year, month, day)).toOption

  // A two-digit year is 20yy unless that would be (more than a year) after the upload, then it is 19yy.
  private def expandYear(raw: String, uploadYear: Int): Int =
    if raw.length == 4 then raw.toInt
    else {
      val candidate = 2000 + raw.toInt
      if candidate > uploadYear + 1 then 1900 + raw.toInt else candidate
    }

  private def parseIso(title: String): Option[LocalDate] =
    isoRegex
      .findAllMatchIn(title)
      .flatMap(m => safeDate(year = m.group(1).toInt, month = m.group(2).toInt, day = m.group(3).toInt))
      .nextOption()

  private def parseNumeric(title: String, uploadYear: Int): Option[LocalDate] =
    numericRegex
      .findAllMatchIn(title)
      .flatMap(m =>
        safeDate(year = expandYear(m.group(3), uploadYear), month = m.group(2).toInt, day = m.group(1).toInt)
      )
      .nextOption()

  private def parseMonthName(title: String, uploadYear: Int, withYear: Boolean): Option[LocalDate] =
    monthNameRegex
      .findAllMatchIn(title)
      .filter(m => Option(m.group(3)).isDefined == withYear)
      .flatMap(m =>
        italianMonths
          .get(m.group(2).toLowerCase)
          .flatMap(month =>
            safeDate(year = Option(m.group(3)).fold(uploadYear)(_.toInt), month = month, day = m.group(1).toInt)
          )
      )
      .nextOption()

  /** First valid date found in the title, by precedence: ISO, numeric, month name with year, month name without year
    * (which takes `uploadYear`).
    */
  def parse(title: String, uploadYear: Int): Option[LocalDate] =
    parseIso(title)
      .orElse(parseNumeric(title, uploadYear))
      .orElse(parseMonthName(title = title, uploadYear = uploadYear, withYear = true))
      .orElse(parseMonthName(title = title, uploadYear = uploadYear, withYear = false))
}

extension (show: Show) {

  /** The title date when present, otherwise the upload date. */
  def eventDate: LocalDate =
    TitleDateParser.parse(show.title, show.uploadDate.getYear).getOrElse(show.uploadDate)

  def eventMonthDay: MonthDay = MonthDay.from(eventDate)
}

/** Selects the shows to celebrate on a given day of the year. */
object ShowCalendar {

  /** Shorter shows are dropped when more than one show falls on the same day. */
  val minDurationSeconds: Int = 5 * 60

  given Ordering[MonthDay] = Ordering.by(monthDay => (monthDay.getMonthValue, monthDay.getDayOfMonth))

  private def dropShortWhenCrowded(dayShows: List[Show]): List[Show] =
    if dayShows.sizeIs > 1 then dayShows.filter(_.duration >= minDurationSeconds) else dayShows

  private def eligible(shows: List[Show]): List[Show] = shows.filterNot(_.isLive)

  /** Candidates of every day of the year (livestreams and short shows of crowded days excluded). */
  def candidates(shows: List[Show]): Map[MonthDay, List[Show]] =
    eligible(shows).groupBy(_.eventMonthDay).view.mapValues(dropShortWhenCrowded).toMap

  /** All candidates, ordered by day of the year and then by show id. */
  def orderedCandidates(shows: List[Show]): List[Show] =
    candidates(shows).toList.sortBy(_._1).flatMap(_._2.sortBy(_.id))

  def forDay(shows: List[Show], day: MonthDay): List[Show] =
    dropShortWhenCrowded(eligible(shows).filter(_.eventMonthDay == day))

  /** Candidates for the given day, or for the current day when none is specified. */
  def forDayOrToday[F[_]: Sync](shows: List[Show], day: Option[MonthDay]): F[List[Show]] =
    day.fold(Sync[F].delay(MonthDay.now()))(_.pure[F]).map(forDay(shows, _))
}
