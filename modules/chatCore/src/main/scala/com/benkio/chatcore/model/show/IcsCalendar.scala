package com.benkio.chatcore.model.show

import java.nio.charset.StandardCharsets
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneOffset

/** Minimal RFC 5545 writer: all-day, yearly-recurring events, one per show. */
object IcsCalendar {

  private val crlf              = "\r\n"
  private val dateFormatter     = DateTimeFormatter.ofPattern("yyyyMMdd")
  private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC)
  private val maxLineOctets     = 75
  private val newlineRegex      = "\r\n|\r|\n"
  private final case class Chunks(done: Vector[String], current: List[String], octets: Int)

  def escape(text: String): String =
    text
      .replace("\\", "\\\\")
      .replace(";", "\\;")
      .replace(",", "\\,")
      .replaceAll(newlineRegex, "\\\\n")

  /** Splits a content line in lines of at most 75 octets (UTF-8), never inside a character; continuation lines start
    * with a space.
    */
  def fold(line: String): List[String] = {
    val codePoints = line.codePoints().toArray.toList.map((cp: Int) => new String(Character.toChars(cp)))
    val chunks     = codePoints.foldLeft(Chunks(Vector.empty, List.empty, 0)) { (acc, codePoint) =>
      val size  = codePoint.getBytes(StandardCharsets.UTF_8).length
      val limit = if acc.done.isEmpty then maxLineOctets else maxLineOctets - 1
      if acc.octets + size > limit then Chunks(acc.done :+ acc.current.reverse.mkString, List(codePoint), size)
      else Chunks(acc.done, codePoint :: acc.current, acc.octets + size)
    }
    (chunks.done :+ chunks.current.reverse.mkString).toList.zipWithIndex.map { case (chunk, index) =>
      if index == 0 then chunk else s" $chunk"
    }
  }

  def unfold(text: String): String = text.replace(s"$crlf ", "")

  def description(show: Show): String =
    (show.description.filter(_.trim.nonEmpty).toList :+ Show.youtubeUrl(show)).mkString("\n")

  def renderEvent(show: Show, dtStamp: Instant): List[String] = List(
    "BEGIN:VEVENT",
    s"UID:${show.id}@sbots",
    s"DTSTAMP:${dateTimeFormatter.format(dtStamp)}",
    s"DTSTART;VALUE=DATE:${dateFormatter.format(show.eventDate)}",
    "RRULE:FREQ=YEARLY",
    "TRANSP:TRANSPARENT",
    s"SUMMARY:${escape(show.title)}",
    s"DESCRIPTION:${escape(description(show))}",
    "END:VEVENT"
  )

  def render(shows: List[Show], dtStamp: Instant): String =
    (List("BEGIN:VCALENDAR", "VERSION:2.0", "PRODID:-//sBots//Shows Calendar//IT", "CALSCALE:GREGORIAN") ++
      shows.flatMap(renderEvent(_, dtStamp)) ++
      List("END:VCALENDAR")).flatMap(fold).map(_ + crlf).mkString
}
