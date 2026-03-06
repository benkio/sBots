package com.benkio.chatcore.model

import com.benkio.chatcore.messagefiltering.*
import com.benkio.chatcore.model.SBotInfo.SBotId
import munit.*

class MessageSpec extends FunSuite {

  test("Message.messageType should return MessageType.Command if the message is a command") {
    val inputMessage: Message = Message(
      messageId = 0,
      date = 0L,
      chatId = ChatId(0L),
      chatType = "test",
      text = Some("/testCommand")
    )
    assertEquals(inputMessage.messageType(SBotId("botId")), MessageType.Command)
  }

  test("Message.messageType should return MessageType.Message if the message is not a command") {
    val inputMessage: Message = Message(
      messageId = 0,
      date = 0L,
      chatId = ChatId(0L),
      chatType = "test",
      text = Some("simple message")
    )
    assertEquals(inputMessage.messageType(SBotId("botId")), MessageType.Message)
    assertEquals(inputMessage.copy(text = None).messageType(SBotId("botId")), MessageType.Message)
  }

  test("Message.messageType should return MessageType.FileRequest if message starts with bot id") {
    val inputMessage: Message = Message(
      messageId = 0,
      date = 0L,
      chatId = ChatId(0L),
      chatType = "test",
      text = Some("rphjb_Schifosi.mp4")
    )
    assertEquals(inputMessage.messageType(SBotId("rphjb")), MessageType.FileRequest)
  }
}
