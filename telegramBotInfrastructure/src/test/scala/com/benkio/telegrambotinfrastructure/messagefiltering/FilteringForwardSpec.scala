package com.benkio.telegrambotinfrastructure.messagefiltering

import telegramium.bots.MessageOriginHiddenUser
import munit.*
import telegramium.bots.Chat
import iozhik.OpenEnum

import telegramium.bots.Message

class FilteringForwardSpec extends FunSuite {
  test("FilteringForward.filter should return true if the disableForward is false") {
    val msg = Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = 0, `type` = "test")
    )
    val msg2 = Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = 0, `type` = "test"),
      forwardOrigin = Some(OpenEnum(MessageOriginHiddenUser(date = 2, senderUserName = "senderUserName")))
    )
    assert(FilteringForward.filter(msg, false))
    assert(FilteringForward.filter(msg2, false))
  }

  test("FilteringForward.filter should return true if the disableForward is true and the message is not a forward") {
    val msg = Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = 0, `type` = "test")
    )
    assert(FilteringForward.filter(msg, true))
  }

  test("FilteringForward.filter should return false if the disableForward is true and the message is a forward") {
    val msg = Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = 0, `type` = "test"),
      forwardOrigin = Some(OpenEnum(MessageOriginHiddenUser(date = 2, senderUserName = "senderUserName")))
    )
    assert(!FilteringForward.filter(msg, true))
  }
}
