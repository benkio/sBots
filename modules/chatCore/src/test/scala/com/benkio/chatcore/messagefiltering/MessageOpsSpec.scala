package com.benkio.chatcore.messagefiltering

import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageType
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.Arbitraries.given
import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop.*

class MessageOpsSpec extends ScalaCheckSuite {

  property("messageType is Command when text starts with '/'") {
    forAll(arbitrary[SBotId], Gen.alphaNumStr) { (botId, rest) =>
      val msg = Message(None, 0L, ChatId.telegram(0L), "test", text = Some("/" + rest))
      assertEquals(msg.messageType(botId), MessageType.Command)
    }
  }

  property("messageType is FileRequest when text starts with botId but not '/'") {
    forAll(arbitrary[SBotId], Gen.alphaNumStr) { (botId, suffix) =>
      val content = botId.value + suffix
      val msg     = Message(None, 0L, ChatId.telegram(0L), "test", text = Some(content))
      assertEquals(msg.messageType(botId), MessageType.FileRequest)
    }
  }

  property("messageType is Message when text is missing or neither command nor file request") {
    forAll(
      arbitrary[SBotId],
      Gen.alphaStr.suchThat(t => !t.startsWith("/") && t.nonEmpty)
    ) { (botId, text) =>
      val safeText =
        if text.startsWith(botId.value) then s"x$text" else text
      val withText = Message(None, 0L, ChatId.telegram(0L), "test", text = Some(safeText))
      val noText   = Message(None, 0L, ChatId.telegram(0L), "test", text = None)
      assertEquals(withText.messageType(botId), MessageType.Message)
      assertEquals(noText.messageType(botId), MessageType.Message)
    }
  }

  property("messageType follows the command syntax of the platform") {
    val irc = CommandSyntax(prefix = "!")
    forAll(arbitrary[SBotId], Gen.alphaNumStr) { (botId, rest) =>
      val command = Message(None, 0L, ChatId(Platform.Irc, "#c"), "test", text = Some("!" + rest))
      val slash   = Message(None, 0L, ChatId(Platform.Irc, "#c"), "test", text = Some("/" + rest))
      assertEquals(command.messageType(botId, irc), MessageType.Command)
      assertNotEquals(slash.messageType(botId, irc), MessageType.Command)
    }
  }

  property("messageType takes the file requests with the marker of the platform") {
    val syntax = CommandSyntax(prefix = "!", fileRequestMarker = "+")
    forAll(arbitrary[SBotId], Gen.alphaNumStr) { (botId, suffix) =>
      val request = Message(None, 0L, ChatId(Platform.Irc, "#c"), "test", text = Some("+" + botId.value + suffix))
      val plain   = Message(None, 0L, ChatId(Platform.Irc, "#c"), "test", text = Some(botId.value + suffix))
      assertEquals(request.messageType(botId, syntax), MessageType.FileRequest)
      assertEquals(plain.messageType(botId, syntax), MessageType.Message)
    }
  }
}
