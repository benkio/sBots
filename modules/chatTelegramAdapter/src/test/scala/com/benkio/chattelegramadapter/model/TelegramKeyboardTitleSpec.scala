package com.benkio.chattelegramadapter.model

import cats.syntax.all.toShow
import com.benkio.chatcore.messagefiltering.getContent
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Trigger
import com.benkio.chatcore.Arbitraries.given
import com.benkio.chatcore.Generators.messageGen
import com.benkio.chattelegramadapter.Generators.commandKeyGen
import munit.ScalaCheckSuite
import org.scalacheck.Prop.*

class TelegramKeyboardTitleSpec extends ScalaCheckSuite {

  property("SearchCommandTelegramKeyboardTitle.build should create the expected formatted title") {
    forAll { (message: Message, sampleTrigger: Trigger) =>
      val content = message.getContent.getOrElse("")
      val result  = SearchCommandTelegramKeyboardTitle.build(message, sampleTrigger)

      assertEquals(
        result.value,
        s"Input:\n$content\n\n${sampleTrigger.show}"
      )
    }
  }

  property("toTelegramKeyboardTitle should return a Search title only for TriggerSearch commands") {
    forAll(commandKeyGen, messageGen) { (commandKey, message) =>
      val content = message.getContent.getOrElse("")
      val result  = TelegramKeyboardTitle.toTelegramKeyboardTitle(message, commandKey)

      commandKey match {
        case CommandKey.TriggerSearch =>
          assertEquals(result, SearchCommandTelegramKeyboardTitle(content))
        case _ =>
          assertEquals(result, IdentityTelegramKeyboardTitle(content))
      }
    }
  }

  property("SearchCommandTelegramKeyboardTitle.extractInput should return the original input") {
    forAll { (message: Message, sampleTrigger: Trigger) =>
      val title = SearchCommandTelegramKeyboardTitle.build(message, sampleTrigger)
      assertEquals(title.extractInput, message.getContent.getOrElse(""))
    }
  }

  property("SearchCommandTelegramKeyboardTitle built title and extractInput should preserve message content") {
    forAll { (originalMessage: Message, sampleTrigger: Trigger) =>
      val title = SearchCommandTelegramKeyboardTitle.build(originalMessage, sampleTrigger)

      assertEquals(title.extractInput, originalMessage.getContent.getOrElse(""))
    }
  }

  property("IdentityTelegramKeyboardTitle.extractInput should return the same string") {
    forAll { (message: Message) =>
      val content = message.getContent.getOrElse("")
      val title   = IdentityTelegramKeyboardTitle(content)
      assertEquals(title.extractInput, content)
    }
  }
}
