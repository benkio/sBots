package com.benkio.telegrambotinfrastructure.model

import com.benkio.telegrambotinfrastructure.messagefiltering.*
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import munit.*

class MessageSpec extends FunSuite {

  test("Message.messageType should return MessageType.Command if the message is a command") {
    val inputMessage: Message = Message(
      messageId = 0,
      date = 0L,
      chatId = ChatId(0L),
      chatType = "test",
      text = Some("/testCommand"),
      caption = None,
      newChatMembers = List.empty,
      leftChatMember = None,
      forwardOrigin = None
    )
    assertEquals(inputMessage.messageType(SBotId("botId")), MessageType.Command)
  }

  test("Message.messageType should return MessageType.Message if the message is not a command") {
    val inputMessage: Message = Message(
      messageId = 0,
      date = 0L,
      chatId = ChatId(0L),
      chatType = "test",
      text = Some("simple message"),
      caption = None,
      newChatMembers = List.empty,
      leftChatMember = None,
      forwardOrigin = None
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
      text = Some("rphjb_Schifosi.mp4"),
      caption = None,
      newChatMembers = List.empty,
      leftChatMember = None,
      forwardOrigin = None
    )
    assertEquals(inputMessage.messageType(SBotId("rphjb")), MessageType.FileRequest)
  }
}

