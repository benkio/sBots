package com.benkio.chatcore.model.reply

import cats.data.NonEmptyList
import com.benkio.chatcore.model.show.Show
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo.SBotId
import munit.FunSuite

import java.time.LocalDate

class PagedListSpec extends FunSuite {

  private val message: Message =
    Message(0, 0L, ChatId.telegram(1L), "private", text = Some("/searchshow some words"))

  private def show(n: Int): Show = Show(
    id = s"id$n",
    botId = SBotId("bot"),
    title = s"title $n",
    uploadDate = LocalDate.of(2020, 1, 1),
    duration = 60,
    description = None,
    isLive = false,
    originAutomaticCaption = None,
    originAutomaticCaptionSrt = Vector.empty,
    timestamp = None
  )

  private def list(count: Int, page: Int): PagedList = PagedList(
    title = "Headline\nechoed text",
    items = (1 to count).toList.map(n => PagedItem.ValueItem(Text(s"item $n"))),
    page = page,
    commandKey = CommandKey.TriggerSearch
  )

  test("a page has up to pageSize items and tells whether there are other pages") {
    val first = list(12, 0)
    val last  = list(12, 2)
    assertEquals(first.pageItems.map(_.label), List("item 1", "item 2", "item 3", "item 4", "item 5"))
    assertEquals(last.pageItems.map(_.label), List("item 11", "item 12"))
    assert(!first.hasPrevious && first.hasNext)
    assert(list(12, 1).hasPrevious && list(12, 1).hasNext)
    assert(last.hasPrevious && !last.hasNext)
    assertEquals(first.totalPages, 3)
    assertEquals(list(5, 0).totalPages, 1)
    assertEquals(list(0, 0).totalPages, 1)
  }

  test("the label of an item is what the user reads: the title, the text, the file name") {
    assertEquals(PagedItem.ValueItem(Text("a text")).label, "a text")
    assertEquals(PagedItem.ValueItem(Markdown("**md**")).label, "**md**")
    assertEquals(PagedItem.ValueItem(Mp3File("dir/audio.mp3")).label, "audio.mp3")
  }

  test("searchTitle gives the number of results, what was typed and what was searched") {
    assertEquals(
      PagedList.searchTitle(message, "some words", 7),
      "Input (7):\n/searchshow some words\n\nsome words"
    )
  }

  test("the builders list what each command found, for the command that can page them") {
    val shows = PagedList.shows(message, "some words", NonEmptyList.of(show(1), show(2)), page = 1)
    assertEquals(shows.commandKey, CommandKey.SearchShow)
    assertEquals(shows.page, 1)
    assertEquals(shows.items.map(_.label), List("title 1", "title 2"))
    assert(shows.title.startsWith("Input (2):"))

    val top = PagedList.topTwenty(List(Mp3File("a.mp3"), Mp3File("b.mp3")), page = 0)
    assertEquals(top.commandKey, CommandKey.TopTwenty)
    assertEquals(top.title, "-----Top 20 Triggers-----")
    assertEquals(top.items.map(_.label), List("a.mp3", "b.mp3"))

    val bundle   = ReplyBundleMessage.textToText("ciao")("hello", "hi")
    val triggers = PagedList.triggers(message, bundle, page = 0)
    assertEquals(triggers.commandKey, CommandKey.TriggerSearch)
    assertEquals(triggers.items.map(_.label).toSet, Set("hello", "hi"))
    assert(triggers.title.startsWith("Input (2):"))
  }

  test("toPlainText lists the numbered items of the page, going on across the pages") {
    assertEquals(
      PagedList.toPlainText(list(12, 1)),
      "Headline\n6. item 6\n7. item 7\n8. item 8\n9. item 9\n10. item 10\npage 2 of 3"
    )
  }

  test("toPlainText has no position when there is a single page") {
    assertEquals(PagedList.toPlainText(list(2, 0)), "Headline\n1. item 1\n2. item 2")
  }

  test("toPlainText of an empty list is the headline") {
    assertEquals(PagedList.toPlainText(list(0, 0)), "Headline")
  }

  test("the page size must be positive") {
    val _ = intercept[IllegalArgumentException](list(1, 0).copy(pageSize = 0))
  }
}
