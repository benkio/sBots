package com.benkio.chatcore.model

import com.benkio.chatcore.messagefiltering.*
import com.benkio.chatcore.model.Mention
import com.benkio.chatcore.model.RepliedMessage
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.TestIds
import munit.*

class MessageSpec extends FunSuite {

  test("Message.messageType should return MessageType.Command if the message is a command") {
    val inputMessage: Message = Message(
      messageId = None,
      date = 0L,
      chatId = TestIds.chatId(0L),
      chatType = "test",
      text = Some("/testCommand")
    )
    assertEquals(inputMessage.messageType(SBotId("botId")), MessageType.Command)
  }

  test("Message.messageType should return MessageType.Message if the message is not a command") {
    val inputMessage: Message = Message(
      messageId = None,
      date = 0L,
      chatId = TestIds.chatId(0L),
      chatType = "test",
      text = Some("simple message")
    )
    assertEquals(inputMessage.messageType(SBotId("botId")), MessageType.Message)
    assertEquals(inputMessage.copy(text = None).messageType(SBotId("botId")), MessageType.Message)
  }

  test("Message.messageType should return MessageType.FileRequest if message starts with bot id") {
    val inputMessage: Message = Message(
      messageId = None,
      date = 0L,
      chatId = TestIds.chatId(0L),
      chatType = "test",
      text = Some("rphjb_Schifosi.mp4")
    )
    assertEquals(inputMessage.messageType(SBotId("rphjb")), MessageType.FileRequest)
  }

  private val bot   = com.benkio.chatcore.model.SBotInfo.SBotName("SBotsBot")
  private val plain = Message(
    messageId = None,
    date = 0L,
    chatId = TestIds.chatId(0L),
    chatType = "test",
    text = Some("hello there")
  )

  test("isDirectedAt: a plain message is not directed at the bot") {
    assert(!plain.isDirectedAt(bot))
  }

  test("isDirectedAt: a mention of the bot, whatever the case and the @, is detected") {
    assert(plain.copy(mentions = List(Mention(name = Some("sbotsbot")))).isDirectedAt(bot))
    assert(plain.copy(mentions = List(Mention(name = Some("@SBOTSBOT")))).isDirectedAt(bot))
    assert(!plain.copy(mentions = List(Mention(name = Some("someoneElse")))).isDirectedAt(bot))
  }

  test("isDirectedAt: a reply to a message of the bot is detected, also with a quote") {
    val reply = RepliedMessage(authorName = Some("SBotsBot"))
    assert(plain.copy(replyTo = Some(reply)).isDirectedAt(bot))
    assert(plain.copy(replyTo = Some(reply), quote = Some("hello")).isDirectedAt(bot))
    assert(!plain.copy(replyTo = Some(RepliedMessage(authorName = Some("alice")))).isDirectedAt(bot))
  }

  test("isDirectedAt: the text opening by calling the bot is detected, a text only containing the name is not") {
    assert(plain.copy(text = Some("SBotsBot: hi")).isDirectedAt(bot))
    assert(plain.copy(text = Some("sbotsbot, hi")).isDirectedAt(bot))
    assert(!plain.copy(text = Some("SBotsBot is great")).isDirectedAt(bot))
    assert(!plain.copy(text = Some("SBotsBots: hi")).isDirectedAt(bot))
  }

  test("anonymisedAuthor: stable per author and salt, not the id, absent without author") {
    val m = plain.copy(authorId = Some("42"))
    assertEquals(m.anonymisedAuthor("s"), m.anonymisedAuthor("s"))
    assertNotEquals(m.anonymisedAuthor("s"), m.anonymisedAuthor("t"))
    assertNotEquals(m.anonymisedAuthor("s"), Some("42"))
    assertEquals(plain.anonymisedAuthor("s"), None)
  }
}
