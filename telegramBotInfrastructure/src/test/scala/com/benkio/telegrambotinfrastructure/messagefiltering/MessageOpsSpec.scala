package com.benkio.telegrambotinfrastructure.messagefiltering

import munit._
import telegramium.bots.{Chat, Message}

class MessageOpsSpec extends FunSuite {

  test("MessageOps.isCommand should return true if the message is a command") {
    val inputMessage: Message = Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = 0, `type` = "test"),
      text = Some("/testCommand")
    )
    assert(MessageOps.isCommand(inputMessage))
  }

  test("MessageOps.isCommand should return false if the message is not a command") {
    val inputMessage: Message = Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = 0, `type` = "test"),
      text = Some("simple message")
    )
    assert(!MessageOps.isCommand(inputMessage))
    assert(!MessageOps.isCommand(inputMessage.copy(text = None)))
  }
}
