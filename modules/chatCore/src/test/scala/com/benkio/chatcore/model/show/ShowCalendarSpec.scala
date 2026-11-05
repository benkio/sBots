package com.benkio.chatcore.model.show

import cats.effect.IO
import com.benkio.chatcore.model.show.ShowTestData.show
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.effect.PropF
import org.scalacheck.Gen

import java.time.LocalDate
import java.time.MonthDay

class ShowCalendarSpec extends CatsEffectSuite with ScalaCheckEffectSuite {

  private val may17 = MonthDay.of(5, 17)

  test("eventDate prefers the title date over the upload date") {
    val s = show(title = "Live 23 Novembre 1999", uploadDate = LocalDate.of(2020, 5, 17))
    assertEquals(s.eventDate, LocalDate.of(1999, 11, 23))
    assertEquals(s.eventMonthDay, MonthDay.of(11, 23))
  }

  test("eventDate falls back to the upload date") {
    val s = show(title = "No date here", uploadDate = LocalDate.of(2020, 5, 17))
    assertEquals(s.eventDate, LocalDate.of(2020, 5, 17))
    assertEquals(s.eventMonthDay, may17)
  }

  test("livestreams are always ignored") {
    val shows = List(show(id = "live", isLive = true), show(id = "vod"))
    assertEquals(ShowCalendar.forDay(shows, may17).map(_.id), List("vod"))
    assertEquals(ShowCalendar.candidates(shows).values.flatten.map(_.id).toList, List("vod"))
  }

  test("a lone short show is kept") {
    val shows = List(show(id = "short", duration = 60))
    assertEquals(ShowCalendar.forDay(shows, may17).map(_.id), List("short"))
  }

  test("short shows are dropped when more shows fall on the same day") {
    val shows = List(
      show(id = "short", duration = ShowCalendar.minDurationSeconds - 1),
      show(id = "long", duration = 3600),
      show(id = "exactly", duration = ShowCalendar.minDurationSeconds)
    )
    assertEquals(ShowCalendar.forDay(shows, may17).map(_.id).toSet, Set("long", "exactly"))
  }

  test("the minimum duration is 5 minutes") {
    assertEquals(ShowCalendar.minDurationSeconds, 300)
  }

  test("on a crowded day the 5 minutes boundary is hardcoded: 299s dropped, 300s and 301s kept") {
    val shows = List(
      show(id = "s299", duration = 299),
      show(id = "s300", duration = 300),
      show(id = "s301", duration = 301)
    )
    assertEquals(ShowCalendar.forDay(shows, may17).map(_.id).toSet, Set("s300", "s301"))
    assertEquals(ShowCalendar.candidates(shows)(may17).map(_.id).toSet, Set("s300", "s301"))
  }

  test("a show between 5 and 10 minutes is kept on a crowded day") {
    val shows = List(show(id = "seven", duration = 7 * 60), show(id = "long", duration = 3600))
    assertEquals(ShowCalendar.forDay(shows, may17).map(_.id).toSet, Set("seven", "long"))
  }

  test("same day of different years counts as the same day") {
    val shows = List(
      show(id = "old", uploadDate = LocalDate.of(2010, 5, 17), duration = 30),
      show(id = "new", uploadDate = LocalDate.of(2024, 5, 17), duration = 3600)
    )
    assertEquals(ShowCalendar.forDay(shows, may17).map(_.id), List("new"))
  }

  test("forDay returns only the shows of that day") {
    val shows = List(show(id = "a"), show(id = "b", uploadDate = LocalDate.of(2020, 6, 1)))
    assertEquals(ShowCalendar.forDay(shows, MonthDay.of(6, 1)).map(_.id), List("b"))
    assertEquals(ShowCalendar.forDay(shows, MonthDay.of(1, 1)), List.empty)
  }

  test("forDay and candidates agree") {
    val shows = List(
      show(id = "a", duration = 10),
      show(id = "b"),
      show(id = "c", uploadDate = LocalDate.of(2020, 6, 1), duration = 10)
    )
    ShowCalendar.candidates(shows).foreach { case (day, dayShows) =>
      assertEquals(ShowCalendar.forDay(shows, day).toSet, dayShows.toSet)
    }
  }

  test("orderedCandidates is ordered by day of the year then id") {
    val shows = List(
      show(id = "z", uploadDate = LocalDate.of(2020, 12, 1)),
      show(id = "b", uploadDate = LocalDate.of(2020, 1, 20)),
      show(id = "a", uploadDate = LocalDate.of(2021, 1, 20))
    )
    assertEquals(ShowCalendar.orderedCandidates(shows).map(_.id), List("a", "b", "z"))
  }

  test("forDayOrToday uses the given day when specified") {
    ShowCalendar
      .forDayOrToday[IO](List(show(id = "a")), Some(may17))
      .map(result => assertEquals(result.map(_.id), List("a")))
  }

  test("forDayOrToday uses the current day when none is specified") {
    val today = LocalDate.now()
    val shows = List(show(id = "today", uploadDate = today, title = "No date"))
    ShowCalendar
      .forDayOrToday[IO](shows, None)
      .map(result => assertEquals(result.map(_.id), List("today")))
  }

  test("property: results come from the input, are never live and are deterministic") {
    val showGen = for {
      id       <- Gen.alphaNumStr.suchThat(_.nonEmpty)
      month    <- Gen.choose(1, 12)
      day      <- Gen.choose(1, 28)
      duration <- Gen.choose(0, 7200)
      isLive   <- Gen.oneOf(true, false)
    } yield show(id = id, uploadDate = LocalDate.of(2020, month, day), duration = duration, isLive = isLive)
    PropF.forAllF(Gen.listOf(showGen)) { (shows: List[Show]) =>
      val result = ShowCalendar.orderedCandidates(shows)
      IO {
        assert(result.forall(shows.contains))
        assert(result.forall(!_.isLive))
        assertEquals(result, ShowCalendar.orderedCandidates(shows))
      }
    }
  }
}
