package com.benkio.chattelegramadapter.model

import com.benkio.chatcore.model.show.CalendarMessage
import com.benkio.chatcore.model.show.ShowTestData.show
import munit.FunSuite

class TelegramCalendarMessageSpec extends FunSuite {

  test("the Telegram text message limit is 4096 characters") {
    assertEquals(TelegramLimits.maxTextMessageLength, 4096)
  }

  test("no shows give no message") {
    assertEquals(TelegramCalendarMessage.render("MyBot", List.empty), None)
  }

  test("a few shows are sent in full as one message") {
    val shows   = (1 to 3).toList.map(i => show(id = s"id$i", title = s"Title $i"))
    val message = TelegramCalendarMessage.render("MyBot", shows).get
    assertEquals(message.value.split("\n").count(_.startsWith("• [")), 3)
    assert(!message.value.contains("more"))
  }

  test("many shows are cut to the Telegram limit, ending with the number of omitted ones") {
    val shows   = (1 to 500).toList.map(i => show(id = f"id$i%04d", title = s"A long enough show title number $i"))
    val message = TelegramCalendarMessage.render("MyBot", shows).get
    val listed  = message.value.split("\n").count(_.startsWith("• ["))
    assert(message.value.length <= TelegramLimits.maxTextMessageLength, message.value.length)
    assert(listed < 500)
    assertEquals(message.value.split("\n").last, s"\\.\\.\\.and ${500 - listed} more")
  }

  test("a message within the limit is left untouched") {
    val message = CalendarMessage.render("MyBot", List(show())).get
    assertEquals(TelegramCalendarMessage.limit(message), message)
  }

  test("the header is kept and the cut is made on whole list blocks") {
    val shows   = (1 to 20).toList.map(i => show(id = f"id$i%02d", title = s"Title $i"))
    val message = CalendarMessage.render("MyBot", shows).get
    val limited = TelegramCalendarMessage.limit(message, maxLength = 300)
    val blocks  = limited.value.split("\n\n").toList
    assert(limited.value.length <= 300, limited.value.length)
    assertEquals(blocks.head, message.value.split("\n\n").head)
    assert(blocks.tail.init.forall(_.startsWith("• [")))
    assertEquals(blocks.last, s"\\.\\.\\.and ${20 - (blocks.size - 2)} more")
  }
}
