package com.benkio.chattelegramadapter.http.telegramreply.messagereply

import com.benkio.chatcore.model.CommandKey
import munit.FunSuite
import telegramium.bots.InlineKeyboardButton

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
}
