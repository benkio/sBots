package com.benkio.chattelegramadapter.http

import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.SBotInfo.SBotName
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*
import telegramium.bots.Chat
import telegramium.bots.Message

class LogTelegramChatSpec extends ScalaCheckSuite {

  private val sampleMessage: Message = Message(
    messageId = 1,
    date = 0,
    chat = Chat(id = 1L, `type` = "private"),
    text = Some("hello"),
    caption = Some("caption")
  )
  private val mediaFile = Mp3File("audio.mp3")
  private val botInfo   = SBotInfo(SBotId("bot"), SBotName("BotName"))

  property("formatErrorText includes message fields, media file and error message") {
    forAll(Gen.alphaStr) { (errMsg: String) =>
      val formatted = LogTelegramChat.formatErrorText(sampleMessage, mediaFile, new RuntimeException(errMsg))
      assert(formatted.contains("hello"))
      assert(formatted.contains("caption"))
      assert(formatted.contains(mediaFile.toString) || formatted.contains("audio.mp3"))
      assert(formatted.contains(errMsg))
    }
  }

  property("formatInfoText prefixes bot name") {
    forAll(Gen.alphaStr) { (msg: String) =>
      assertEquals(LogTelegramChat.formatInfoText(msg, botInfo), s"[${botInfo.botName}] $msg")
    }
  }
}
