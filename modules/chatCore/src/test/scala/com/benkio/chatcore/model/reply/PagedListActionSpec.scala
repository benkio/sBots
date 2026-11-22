package com.benkio.chatcore.model.reply

import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.PagedListNavigation
import munit.FunSuite

class PagedListActionSpec extends FunSuite {

  private val actions: List[PagedListAction] = List(
    PagedListAction.PreviousPage(3, CommandKey.TriggerList),
    PagedListAction.NextPage(0, CommandKey.TopTwenty),
    PagedListAction.Media("rphjb_NudoFrocio.mp3"),
    PagedListAction.Media("a-name-with-dashes.mp3"),
    PagedListAction.Show("abc-123", None),
    PagedListAction.Show("abc-123", Some("1m2s")),
    PagedListAction.Trigger("francesismo"),
    PagedListAction.Trigger("a-name-with-dashes")
  )

  test("fromId should decode what toId encoded") {
    actions.foreach(action => assertEquals(action.toId.flatMap(PagedListAction.fromId(_)), Some(action)))
  }

  test("toId should be at most maxIdLength characters, None for a longer action") {
    val atLimit = PagedListAction.Media("a" * (PagedListAction.maxIdLength - "media-".length))
    assertEquals(atLimit.toId.map(_.length), Some(PagedListAction.maxIdLength))
    assertEquals(PagedListAction.Media("a" * PagedListAction.maxIdLength).toId, None)
  }

  test("fromId should reject an id longer than maxIdLength") {
    assertEquals(PagedListAction.fromId("media-" + "a" * PagedListAction.maxIdLength), None)
  }

  test("fromId should reject bad ids") {
    List("", "unknown-thing", "nextPage-notACommand-1", "nextPage-triggerlist-x", "previousPage").foreach(id =>
      assertEquals(PagedListAction.fromId(id), None, id)
    )
  }

  test("newPage should move forward, stop at the first page going back and be None for the picks") {
    assertEquals(PagedListNavigation.newPage(PagedListAction.NextPage(2, CommandKey.TriggerList)), Some(3))
    assertEquals(PagedListNavigation.newPage(PagedListAction.PreviousPage(2, CommandKey.TriggerList)), Some(1))
    assertEquals(PagedListNavigation.newPage(PagedListAction.PreviousPage(0, CommandKey.TriggerList)), Some(0))
    assertEquals(PagedListNavigation.newPage(PagedListAction.Media("a.mp3")), None)
  }
}
