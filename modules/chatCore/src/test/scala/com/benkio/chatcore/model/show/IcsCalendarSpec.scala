package com.benkio.chatcore.model.show

import com.benkio.chatcore.model.show.ShowTestData.show
import munit.FunSuite
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll

import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate

class IcsCalendarSpec extends FunSuite with ScalaCheckSuite {

  private val dtStamp = Instant.parse("2026-11-05T10:15:30Z")

  private def octets(line: String): Int = line.getBytes(StandardCharsets.UTF_8).length

  test("escape handles backslash, semicolon, comma and newlines") {
    assertEquals(IcsCalendar.escape("a\\b;c,d\ne\r\nf"), "a\\\\b\\;c\\,d\\ne\\nf")
  }

  test("fold leaves short lines untouched") {
    assertEquals(IcsCalendar.fold("short"), List("short"))
  }

  test("fold splits long lines in at most 75 octets with a leading space on continuations") {
    val folded = IcsCalendar.fold("x" * 200)
    assertEquals(folded.head.length, 75)
    assert(folded.tail.forall(_.startsWith(" ")))
    assert(folded.forall(octets(_) <= 75))
  }

  test("fold never splits multibyte characters") {
    val line   = "è" * 100 + "😀" * 30
    val folded = IcsCalendar.fold(line)
    assert(folded.forall(octets(_) <= 75))
    assertEquals(IcsCalendar.unfold(folded.mkString("\r\n")), line)
  }

  property("unfold(fold(s)) == s and every line fits 75 octets") {
    val piece = Gen.oneOf(Gen.alphaNumChar.map(_.toString), Gen.oneOf("\u00e8", "\u20ac", " ", "\ud83d\ude00"))
    forAll(Gen.listOf(piece).map(_.mkString)) { (s: String) =>
      val folded = IcsCalendar.fold(s)
      folded.forall(octets(_) <= 75) && IcsCalendar.unfold(folded.mkString("\r\n")) == s
    }
  }

  test("description is the show description followed by the YouTube link") {
    assertEquals(
      IcsCalendar.description(show(id = "xyz", description = Some("Hello"))),
      "Hello\nhttps://www.youtube.com/watch?v=xyz"
    )
  }

  test("description is only the link when the show has no description") {
    assertEquals(
      IcsCalendar.description(show(id = "xyz", description = None)),
      "https://www.youtube.com/watch?v=xyz"
    )
  }

  test("renderEvent produces a yearly all-day event with the title date") {
    val event = IcsCalendar.renderEvent(
      show(id = "xyz", title = "Live 23 Novembre 1999, parte 1", uploadDate = LocalDate.of(2020, 5, 17)),
      dtStamp
    )
    assertEquals(
      event,
      List(
        "BEGIN:VEVENT",
        "UID:xyz@sbots",
        "DTSTAMP:20261105T101530Z",
        "DTSTART;VALUE=DATE:19991123",
        "RRULE:FREQ=YEARLY",
        "TRANSP:TRANSPARENT",
        "SUMMARY:Live 23 Novembre 1999\\, parte 1",
        "DESCRIPTION:A description\\nhttps://www.youtube.com/watch?v=xyz",
        "END:VEVENT"
      )
    )
  }

  test("render wraps the events in a calendar with CRLF line endings") {
    val result = IcsCalendar.render(List(show(id = "a"), show(id = "b")), dtStamp)
    assert(result.startsWith("BEGIN:VCALENDAR\r\nVERSION:2.0\r\n"))
    assert(result.endsWith("END:VEVENT\r\nEND:VCALENDAR\r\n"))
    assertEquals(result.split("\r\n").count(_ == "BEGIN:VEVENT"), 2)
    assertEquals(result.split("\r\n").count(_ == "END:VEVENT"), 2)
    assert(!result.replace("\r\n", "").contains("\n"))
  }

  test("render of no shows is an empty calendar") {
    assertEquals(
      IcsCalendar.render(List.empty, dtStamp),
      "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//sBots//Shows Calendar//IT\r\nCALSCALE:GREGORIAN\r\nEND:VCALENDAR\r\n"
    )
  }

  test("render folds long descriptions within 75 octets per line") {
    val result = IcsCalendar.render(List(show(description = Some("àèìòù " * 100))), dtStamp)
    assert(result.split("\r\n").forall(octets(_) <= 75))
  }
}
