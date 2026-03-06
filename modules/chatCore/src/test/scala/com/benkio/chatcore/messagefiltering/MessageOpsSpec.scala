package com.benkio.chatcore.messagefiltering

import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageType
import com.benkio.chatcore.model.SBotInfo.SBotId
import munit.*

class MessageOpsSpec extends FunSuite {

  test("MessageOps.messageType should return MessageType.Command if the message is a command") {
    val inputMessage: Message = Message(
      messageId = 0,
      date = 0L,
      chatId = ChatId(0L),
      chatType = "test",
      text = Some("/testCommand")
    )
    assert(inputMessage.messageType(SBotId("botId")) == MessageType.Command)
  }

  test("MessageOps.messageType should return MessageType.Message if the message is not a command") {
    val inputMessage: Message = Message(
      messageId = 0,
      date = 0L,
      chatId = ChatId(0L),
      chatType = "test",
      text = Some("simple message")
    )
    assert(inputMessage.messageType(SBotId("botId")) == MessageType.Message)
    assert(inputMessage.copy(text = None).messageType(SBotId("botId")) == MessageType.Message)
  }

  test(
    "MessageOps.messageType should return MessageType.FileRequest if the start of the message matches the input telegram bot id"
  ) {
    val inputMessage: Message = Message(
      messageId = 0,
      date = 0L,
      chatId = ChatId(0L),
      chatType = "test",
      text = Some("rphjb_Schifosi.mp4")
    )
    assert(inputMessage.messageType(SBotId("rphjb")) == MessageType.FileRequest)
  }
}
