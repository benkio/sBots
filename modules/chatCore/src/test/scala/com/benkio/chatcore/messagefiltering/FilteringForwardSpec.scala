package com.benkio.chatcore.messagefiltering

import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import munit.*

class FilteringForwardSpec extends FunSuite {
  test("FilteringForward.filter should return true if the disableForward is false") {
    val msg = Message(
      messageId = 0,
      date = 0L,
      chatId = ChatId(0L),
      chatType = "test"
    )
    val msg2 = Message(
      messageId = 0,
      date = 0L,
      chatId = ChatId(0L),
      chatType = "test",
      isForward = true
    )
    assert(FilteringForward.filter(msg, false))
    assert(FilteringForward.filter(msg2, false))
  }

  test("FilteringForward.filter should return true if the disableForward is true and the message is not a forward") {
    val msg = Message(
      messageId = 0,
      date = 0L,
      chatId = ChatId(0L),
      chatType = "test"
    )
    assert(FilteringForward.filter(msg, true))
  }

  test("FilteringForward.filter should return false if the disableForward is true and the message is a forward") {
    val msg = Message(
      messageId = 0,
      date = 0L,
      chatId = ChatId(0L),
      chatType = "test",
      isForward = true
    )
    assert(!FilteringForward.filter(msg, true))
  }
}
