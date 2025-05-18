package com.benkio.telegrambotinfrastructure.messagefiltering

import munit.*
import telegramium.bots.Chat
import telegramium.bots.Message

class MessageOpsSpec extends FunSuite {

  test("MessageOps.isCommand should return true if the message is a command") {
    val inputMessage: Message = Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = 0, `type` = "test"),
      text = Some("/testCommand")
    )
    assert(inputMessage.isCommand)
  }

  test("MessageOps.isCommand should return false if the message is not a command") {
    val inputMessage: Message = Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = 0, `type` = "test"),
      text = Some("simple message")
    )
    assert(!inputMessage.isCommand)
    assert(!inputMessage.copy(text = None).isCommand)
  }
}
