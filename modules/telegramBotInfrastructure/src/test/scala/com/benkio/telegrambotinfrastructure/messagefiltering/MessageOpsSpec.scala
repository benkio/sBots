package com.benkio.telegrambotinfrastructure.messagefiltering

import com.benkio.telegrambotinfrastructure.model.MessageType
import munit.*
import telegramium.bots.Chat
import telegramium.bots.Message

class MessageOpsSpec extends FunSuite {

  test("MessageOps.messageType should return MessageType.Command if the message is a command") {
    val inputMessage: Message = Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = 0, `type` = "test"),
      text = Some("/testCommand")
    )
    assert(inputMessage.messageType("botId") == MessageType.Command)
  }

  test("MessageOps.messageType should return MessageType.Message if the message is not a command") {
    val inputMessage: Message = Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = 0, `type` = "test"),
      text = Some("simple message")
    )
    assert(inputMessage.messageType("botId") == MessageType.Message)
    assert(inputMessage.copy(text = None).messageType("botId") == MessageType.Message)
  }

  test(
    "MessageOps.messageType should return MessageType.FileRequest if the start of the message matches the input telegram bot prefix"
  ) {
    val inputMessage: Message = Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = 0, `type` = "test"),
      text = Some("rphjb_Schifosi.mp4")
    )
    assert(inputMessage.messageType("rphjb_") == MessageType.FileRequest)
  }
}
