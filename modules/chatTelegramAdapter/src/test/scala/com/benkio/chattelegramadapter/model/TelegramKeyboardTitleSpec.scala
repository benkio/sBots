package com.benkio.chattelegramadapter.model

import cats.syntax.all.toShow
import com.benkio.chatcore.messagefiltering.getContent
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Trigger
import com.benkio.chattelegramadapter.Arbitraries.given
import munit.ScalaCheckSuite
import org.scalacheck.Prop.*

class TelegramKeyboardTitleSpec extends ScalaCheckSuite {

  property("SearchCommandTelegramKeyboardTitle.build should create the expected formatted title") {
    forAll { (message: Message, sampleTrigger: Trigger) =>
      val content = message.getContent.getOrElse("")
      val count   = 3
      val result  = SearchCommandTelegramKeyboardTitle.build(message, sampleTrigger, count)

      assertEquals(
        result.value,
        s"Input ($count):\n$content\n\n${sampleTrigger.show}"
      )
    }
  }

  property("toTelegramKeyboardTitle should return a Search title only for TriggerSearch commands") {
    forAll { (commandKey: CommandKey, message: Message) =>
      val content = message.getContent.getOrElse("")
      val result  = TelegramKeyboardTitle.toTelegramKeyboardTitle(message, commandKey)

      commandKey match {
        case CommandKey.TriggerSearch | CommandKey.SearchShow =>
          assertEquals(result, SearchCommandTelegramKeyboardTitle(content))
        case _ =>
          assertEquals(result, IdentityTelegramKeyboardTitle(content))
      }
    }
  }

  property("SearchCommandTelegramKeyboardTitle.extractInput should return the user input for command messages") {
    forAll { (message: Message, sampleTrigger: Trigger) =>
      val originalContent = message.getContent.getOrElse("").trim
      val query           = if originalContent.nonEmpty then originalContent else "query words"
      val commandMessage  = message.copy(text = Some(s"/searchshow@SomeBot $query"), caption = None)
      val title           = SearchCommandTelegramKeyboardTitle.build(commandMessage, sampleTrigger, valuesCount = 7)
      assertEquals(title.extractInput, query)
    }
  }

  property("SearchCommandTelegramKeyboardTitle.extractInput should preserve plain non-command content") {
    forAll { (originalMessage: Message, sampleTrigger: Trigger) =>
      val originalContent = originalMessage.getContent.getOrElse("").trim
      val plainInput      =
        if originalContent.startsWith("/") then s"plain $originalContent"
        else if originalContent.nonEmpty then originalContent
        else "plain input"
      val plainMessage = originalMessage.copy(text = Some(plainInput), caption = None)
      val title        = SearchCommandTelegramKeyboardTitle.build(plainMessage, sampleTrigger, valuesCount = 7)
      assertEquals(title.extractInput, plainInput)
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
