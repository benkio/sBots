package com.benkio.chatdiscordadapter.outbound

import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.PagedItem
import com.benkio.chatcore.model.reply.PagedList
import com.benkio.chatcore.model.reply.PagedListAction
import com.benkio.chatcore.model.reply.TelegramSticker
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatdiscordadapter.model.DiscordLimits
import munit.FunSuite

class DiscordPagedListLayoutSpec extends FunSuite {

  private def list(count: Int, page: Int = 0, command: CommandKey = CommandKey.TopTwenty): PagedList =
    PagedList(
      title = "Top",
      items = (1 to count).toList.map(i => PagedItem.ValueItem(Mp3File(s"file$i.mp3"))),
      page = page,
      commandKey = command
    )

  test("a page has a row of pick buttons labelled with the numbers of the text and a navigation row") {
    val message = DiscordPagedListLayout.render(list(count = 12, page = 1))
    assertEquals(message.rows.map(_.map(_.label)), List(List("6", "7", "8", "9", "10"), List("Previous", "Next")))
    assert(message.text.contains("6. file6.mp3"))
  }

  test("the ids are the ones of the actions") {
    val message = DiscordPagedListLayout.render(list(count = 12, page = 1))
    assertEquals(
      message.rows.head.map(b => PagedListAction.fromId(b.id)),
      (6 to 10).toList.map(i => Some(PagedListAction.Media(s"file$i.mp3")))
    )
    assertEquals(
      message.rows(1).map(b => PagedListAction.fromId(b.id)),
      List(
        Some(PagedListAction.PreviousPage(1, CommandKey.TopTwenty)),
        Some(PagedListAction.NextPage(1, CommandKey.TopTwenty))
      )
    )
  }

  test("previous is disabled on the first page and next on the last") {
    assertEquals(DiscordPagedListLayout.render(list(count = 12)).rows(1).map(_.disabled), List(true, false))
    assertEquals(DiscordPagedListLayout.render(list(count = 12, page = 2)).rows(1).map(_.disabled), List(false, true))
  }

  test("a single page has no navigation row") {
    assertEquals(DiscordPagedListLayout.render(list(count = 3)).rows.map(_.size), List(3))
  }

  test("trigger candidates are pick buttons and the one-page list has no navigation row") {
    val suggestions = PagedList.triggerSuggestions("fransesimo", List("francesismo", "francofonia"))
    val message     = DiscordPagedListLayout.render(suggestions)
    assertEquals(message.rows.size, 1)
    assertEquals(
      message.rows.head.map(b => PagedListAction.fromId(b.id)),
      List(Some(PagedListAction.Trigger("francesismo")), Some(PagedListAction.Trigger("francofonia")))
    )
    assertEquals(
      message.text,
      "No exact match for 'fransesimo'. Did you mean:\n1. francesismo\n2. francofonia"
    )
  }

  test("media Discord cannot send, like Telegram stickers, are left out of the text and the buttons") {
    val list = PagedList(
      title = "Top",
      items = List(
        PagedItem.ValueItem(Mp3File("a.mp3")),
        PagedItem.ValueItem(TelegramSticker("b.sticker")),
        PagedItem.ValueItem(Mp3File("c.mp3"))
      ),
      page = 0,
      commandKey = CommandKey.TopTwenty
    )
    val message = DiscordPagedListLayout.render(list)
    assertEquals(message.text, "Top\n1. a.mp3\n2. c.mp3")
    assertEquals(message.rows.head.map(_.label), List("1", "2"))
    assertEquals(
      message.rows.head.map(b => PagedListAction.fromId(b.id)),
      List(Some(PagedListAction.Media("a.mp3")), Some(PagedListAction.Media("c.mp3")))
    )
  }

  test("a page past the end after filtering is clamped to the last page") {
    val items =
      PagedItem.ValueItem(TelegramSticker("s.sticker")) :: (1 to 10).toList.map(i =>
        PagedItem.ValueItem(Mp3File(s"f$i.mp3"))
      )
    val message = DiscordPagedListLayout.render(PagedList("Top", items, page = 2, commandKey = CommandKey.TopTwenty))
    assert(message.text.contains("page 2 of 2"))
    assertEquals(message.rows.last.map(_.disabled), List(false, true))
  }

  test("the texts have nothing to pick, there is no empty row") {
    val texts = PagedList("t", List(PagedItem.ValueItem(Text("a"))), 0, CommandKey.TriggerSearch)
    assertEquals(DiscordPagedListLayout.render(texts).rows, List.empty)
  }

  test("at most 5 buttons per row, at most 2 rows, the ids and the text within the limits") {
    val message = DiscordPagedListLayout.render(list(count = 50, page = 3))
    assert(message.rows.size <= 2 && message.rows.forall(_.size <= 5))
    assert(message.rows.flatten.forall(_.id.length <= PagedListAction.maxIdLength))
    assert(message.rows.flatten.forall(_.label.length <= DiscordPagedListLayout.maxLabelLength))
    val long = PagedList("t" * 3000, List(PagedItem.ValueItem(Mp3File("a.mp3"))), 0, CommandKey.TopTwenty)
    assertEquals(DiscordPagedListLayout.render(long).text.length, DiscordLimits.maxTextMessageLength)
  }
}
