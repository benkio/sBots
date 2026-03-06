package com.benkio.telegrambotinfrastructure.messagefiltering

import com.benkio.telegrambotinfrastructure.model.ChatId
import com.benkio.telegrambotinfrastructure.model.Message
import iozhik.OpenEnum
import munit.*
import telegramium.bots.MessageOriginHiddenUser

class FilteringForwardSpec extends FunSuite {
  test("FilteringForward.filter should return true if the disableForward is false") {
    val msg = Message(
      messageId = 0,
      date = 0L,
      chatId = ChatId(0L),
      chatType = "test",
      text = None,
      caption = None,
      newChatMembers = List.empty,
      leftChatMember = None,
      forwardOrigin = None
    )
    val msg2 = Message(
      messageId = 0,
      date = 0L,
      chatId = ChatId(0L),
      chatType = "test",
      text = None,
      caption = None,
      newChatMembers = List.empty,
      leftChatMember = None,
      forwardOrigin = Some(OpenEnum(MessageOriginHiddenUser(date = 2, senderUserName = "senderUserName")))
    )
    assert(FilteringForward.filter(msg, false))
    assert(FilteringForward.filter(msg2, false))
  }

  test("FilteringForward.filter should return true if the disableForward is true and the message is not a forward") {
    val msg = Message(
      messageId = 0,
      date = 0L,
      chatId = ChatId(0L),
      chatType = "test",
      text = None,
      caption = None,
      newChatMembers = List.empty,
      leftChatMember = None,
      forwardOrigin = None
    )
    assert(FilteringForward.filter(msg, true))
  }

  test("FilteringForward.filter should return false if the disableForward is true and the message is a forward") {
    val msg = Message(
      messageId = 0,
      date = 0L,
      chatId = ChatId(0L),
      chatType = "test",
      text = None,
      caption = None,
      newChatMembers = List.empty,
      leftChatMember = None,
      forwardOrigin = Some(OpenEnum(MessageOriginHiddenUser(date = 2, senderUserName = "senderUserName")))
    )
    assert(!FilteringForward.filter(msg, true))
  }
}
