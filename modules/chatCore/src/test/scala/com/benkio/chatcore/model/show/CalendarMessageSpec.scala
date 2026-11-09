package com.benkio.chatcore.model.show

import com.benkio.chatcore.model.show.ShowTestData.show
import munit.FunSuite

import java.time.LocalDate

class CalendarMessageSpec extends FunSuite {

  test("no shows give no message") {
    assertEquals(CalendarMessage.render("MyBot", List.empty), None)
  }

  test("one show gives a message with the header, the bot name and the link") {
    val message = CalendarMessage.render("My_Bot", List(show(id = "abc", title = "Hello"))).get
    assertEquals(
      message.value.split("\n").toList,
      List(
        "📅 My\\_Bot: oggi nel calendario",
        "",
        "• [Hello](https://www.youtube.com/watch?v=abc)"
      )
    )
  }

  test("N shows give exactly one message with one list line per show") {
    val shows   = (1 to 5).toList.map(i => show(id = s"id$i", title = s"Title $i"))
    val message = CalendarMessage.render("MyBot", shows).get
    assertEquals(message.value.split("\n").count(_.startsWith("• [")), 5)
  }

  test("shows are ordered by upload date and then id") {
    val shows = List(
      show(id = "b", title = "B", uploadDate = LocalDate.of(2020, 1, 1)),
      show(id = "z", title = "Z", uploadDate = LocalDate.of(2019, 1, 1)),
      show(id = "a", title = "A", uploadDate = LocalDate.of(2020, 1, 1))
    )
    val lines =
      CalendarMessage.render("MyBot", shows).get.value.split("\n").toList.filter(_.startsWith("• ["))
    assertEquals(lines.map(_.drop(3).takeWhile(_ != ']')), List("Z", "A", "B"))
  }

  test("special characters of the title are escaped") {
    val title = "a_b*c[d]e(f)g~h`i>j#k+l-m=n|o{p}q.r!s\\t"
    val line  = CalendarMessage.render("MyBot", List(show(title = title))).get.value.split("\n").last
    assertEquals(
      line,
      "• [a\\_b\\*c\\[d\\]e\\(f\\)g\\~h\\`i\\>j\\#k\\+l\\-m\\=n\\|o\\{p\\}q\\.r\\!s\\\\t](https://www.youtube.com/watch?v=abc)"
    )
  }

  test("the whole message is returned, however long") {
    val shows   = (1 to 500).toList.map(i => show(id = f"id$i%04d", title = s"A long enough show title number $i"))
    val message = CalendarMessage.render("MyBot", shows).get
    assertEquals(message.value.split("\n").count(_.startsWith("• [")), 500)
    assert(message.value.length > 4096)
  }

  test("a fire on a given day sends one message with the shows of that day only") {
    val shows = List(
      show(id = "may", title = "17 maggio 2019"),
      show(id = "may2", title = "Second 2018-05-17"),
      show(id = "other", title = "11 gennaio 2008")
    )
    val message = CalendarMessage
      .render("MyBot", ShowCalendar.forDay(shows, java.time.MonthDay.of(5, 17)))
      .get
    assertEquals(message.value.split("\n").count(_.startsWith("• [")), 2)
    assert(!message.value.contains("other"))
    assertEquals(
      CalendarMessage.render("MyBot", ShowCalendar.forDay(shows, java.time.MonthDay.of(7, 7))),
      None
    )
  }
}
