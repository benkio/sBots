package com.benkio.telegrambotinfrastructure.model

import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.TextTriggerValue._
import munit.FunSuite

class TriggersSpec extends FunSuite {

  test("matchValue should return true when source contains StringTextTriggerValue") {
    assert(TextTriggerValue.matchValue(StringTextTriggerValue("match"), "source with match) it"))
  }
  test("matchValue should return true when source has a match of RegexpTextTriggerValue") {
    assert(TextTriggerValue.matchValue(RegexTextTriggerValue("m[ae]tch".r), "this is a test with a match"))
  }

  test("matchValue should return false when source doesn't contains StringTextTriggerValue") {
    assert(!TextTriggerValue.matchValue(StringTextTriggerValue("no match"), "source without match it"))
  }
  test("matchValue should return false when source has not a match of RegexpTextTriggerValue") {
    assert(!TextTriggerValue.matchValue(RegexTextTriggerValue("m[io]tch".r), "this is a test without a match"))
  }

  test("ShowInstance of TextTriggerValue should return the expected string") {
    val expectedStringValue             = "test trigger"
    val expectedRegexValue              = "test [rR]egex(p)?".r
    val stringTrigger: TextTriggerValue = StringTextTriggerValue(expectedStringValue)
    val regexTrigger: TextTriggerValue  = RegexTextTriggerValue(expectedRegexValue)

    assertEquals(stringTrigger.show, expectedStringValue)
    assertEquals(regexTrigger.show, expectedRegexValue.toString)

  }

  test("ShowInstance of Trigger should return the expected string") {
    val textTrigger: Trigger = TextTrigger(
      StringTextTriggerValue("textTriggerValue"),
      RegexTextTriggerValue("regexTriggerValue".r)
    )
    val messageLengthTrigger: Trigger = MessageLengthTrigger(42)
    val newMemberTrigger: Trigger     = NewMemberTrigger
    val commandTrigger: Trigger       = CommandTrigger("/testcommand")

    assertEquals(textTrigger.show, "textTriggerValue\nregexTriggerValue")
    assertEquals(messageLengthTrigger.show, "Trigger when the length of message exceed 42")
    assertEquals(newMemberTrigger.show, "Trigger on new member joining a group")
    assertEquals(commandTrigger.show, "/testcommand")

  }
}
