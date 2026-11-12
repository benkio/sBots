package com.benkio.chattelegramadapter.http.telegramreply.messagereply

import cats.data.NonEmptyList
import com.benkio.chatcore.model.reply.GifFile
import com.benkio.chatcore.model.reply.Markdown
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.PagedItem
import com.benkio.chatcore.model.reply.PagedList
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.show.Show
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chattelegramadapter.model.SearchCommandTelegramKeyboardTitle
import munit.FunSuite
import telegramium.bots.InlineKeyboardButton

import java.time.LocalDate
import scala.concurrent.duration.*

class KeyboardReplySpec extends FunSuite {

  test("paginationButtons should not show next when current page already contains the last elements") {
    val buttons = KeyboardReply.paginationButtons(
      page = 0,
      perPage = 5,
      totalElems = 4,
      commandKey = CommandKey.SearchShow
    )

    assertEquals(buttons, List.empty[InlineKeyboardButton])
  }

  test("paginationButtons should show next when there is another page") {
    val buttons = KeyboardReply.paginationButtons(
      page = 0,
      perPage = 5,
      totalElems = 6,
      commandKey = CommandKey.SearchShow
    )

    assertEquals(
      buttons.map(_.callbackData),
      List(Some("nextPage-searchshow-0"))
    )
  }

  test("paginationButtons should show only prev on the last page") {
    val buttons = KeyboardReply.paginationButtons(
      page = 1,
      perPage = 5,
      totalElems = 6,
      commandKey = CommandKey.SearchShow
    )

    assertEquals(
      buttons.map(_.callbackData),
      List(Some("previousPage-searchshow-1"))
    )
  }

  private val message: Message = Message(None, 0L, ChatId.telegram(1L), "private", text = Some("/cmd words"))

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
    timestamp = Some(5.seconds)
  )

  test("toKeyboard of the shows is the keyboard Telegram always had for the shows") {
    val shows = NonEmptyList.fromListUnsafe((1 to 8).toList.map(show))
    List(0, 1).foreach { page =>
      val keyboard = KeyboardReply.toKeyboard(PagedList.shows(message, "words", shows, page))
      assertEquals(
        keyboard.inlineKeyboard,
        KeyboardReply.buildInlineKeyboard(data = shows.toList, page = page, commandKey = CommandKey.SearchShow)
      )
      assertEquals(keyboard.keyboardTitle, SearchCommandTelegramKeyboardTitle.build(message, "words", 8).value)
    }
  }

  test("toKeyboard of the values is the keyboard Telegram always had for the triggers and the top twenty") {
    val values: List[ReplyValue] = List(Text("a text"), Markdown("**md**"), Mp3File("a.mp3"), GifFile("aGif.mp4"))
    val medias: List[MediaFile]  = (1 to 7).toList.map(n => Mp3File(s"m$n.mp3"))
    val triggers                 = PagedList(
      title = "title",
      items = values.map(PagedItem.ValueItem(_)),
      page = 0,
      commandKey = CommandKey.TriggerSearch
    )
    assertEquals(
      KeyboardReply.toKeyboard(triggers).inlineKeyboard,
      KeyboardReply.buildInlineKeyboard(data = values, page = 0, commandKey = CommandKey.TriggerSearch)
    )
    List(0, 1).foreach { page =>
      val top = KeyboardReply.toKeyboard(PagedList.topTwenty(medias, page))
      assertEquals(
        top.inlineKeyboard,
        KeyboardReply.buildInlineKeyboard(data = medias, page = page, commandKey = CommandKey.TopTwenty)
      )
      assertEquals(top.keyboardTitle, "-----Top 20 Triggers-----")
    }
  }

  test("keyboardOf gives the keyboard of a keyboard or of a paged list, and nothing for the other values") {
    val pagedList = PagedList.topTwenty(List(Mp3File("a.mp3")), page = 0)
    val keyboard  = KeyboardReply.toKeyboard(pagedList)
    assertEquals(KeyboardReply.keyboardOf(keyboard), Some(keyboard))
    assertEquals(KeyboardReply.keyboardOf(pagedList), Some(keyboard))
    assertEquals(KeyboardReply.keyboardOf(Text("no keyboard")), None)
  }
}
