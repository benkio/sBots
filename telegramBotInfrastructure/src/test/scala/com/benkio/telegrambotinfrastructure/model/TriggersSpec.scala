package com.benkio.telegrambotinfrastructure.model

import scala.util.matching.Regex
import cats.implicits.*
import io.circe.parser.decode
import io.circe.syntax.*

import munit.FunSuite

class TriggersSpec extends FunSuite {

  test("matchValue should return true when source contains StringTextTriggerValue") {
    assert(TextTriggerValue.matchValue(StringTextTriggerValue("match"), "source with match) it"))
  }
  test("matchValue should return true when source has a match of RegexpTextTriggerValue") {
    assert(TextTriggerValue.matchValue(RegexTextTriggerValue("m[ae]tch".r, 7), "this is a test with a match"))
  }

  test("matchValue should return false when source doesn't contains StringTextTriggerValue") {
    assert(!TextTriggerValue.matchValue(StringTextTriggerValue("no match"), "source without match it"))
  }
  test("matchValue should return false when source has not a match of RegexpTextTriggerValue") {
    assert(!TextTriggerValue.matchValue(RegexTextTriggerValue("m[io]tch".r, 6), "this is a test without a match"))
  }

  test("ShowInstance of TextTriggerValue should return the expected string") {
    val expectedStringValue             = "test trigger"
    val expectedRegexValue              = "test [rR]egex(p)?".r
    val stringTrigger: TextTriggerValue = StringTextTriggerValue(expectedStringValue)
    val regexTrigger: TextTriggerValue  = RegexTextTriggerValue(expectedRegexValue, 10)

    assertEquals(stringTrigger.show, expectedStringValue)
    assertEquals(regexTrigger.show, expectedRegexValue.toString)

  }

  test("ShowInstance of Trigger should return the expected string") {
    val textTrigger: Trigger = TextTrigger(
      StringTextTriggerValue("textTriggerValue"),
      RegexTextTriggerValue("regexTriggerValue".r, 17)
    )
    val messageLengthTrigger: Trigger = MessageLengthTrigger(42)
    val newMemberTrigger: Trigger     = NewMemberTrigger
    val leftMemberTrigger: Trigger    = LeftMemberTrigger
    val commandTrigger: Trigger       = CommandTrigger("/testcommand")

    assertEquals(textTrigger.show, "textTriggerValue\nregexTriggerValue")
    assertEquals(messageLengthTrigger.show, "trigger when the length of message exceed 42")
    assertEquals(newMemberTrigger.show, "trigger on new member joining a group")
    assertEquals(leftMemberTrigger.show, "trigger when a member leaves a group")
    assertEquals(commandTrigger.show, "/testcommand")

  }

  test("triggerLongestString should return the expected longest trigger") {
    val messageLengthTrigger: Trigger   = MessageLengthTrigger(42)
    val newMemberTrigger: Trigger       = NewMemberTrigger
    val leftMemberTrigger: Trigger      = LeftMemberTrigger
    val commandTrigger: Trigger         = CommandTrigger("/testcommand")
    val stringTrigger: TextTriggerValue = StringTextTriggerValue("test trigger")
    val regexTrigger: TextTriggerValue  = RegexTextTriggerValue("test [rR]egex(p)?".r, 10)

    assertEquals(Trigger.triggerLongestString(newMemberTrigger), 0)
    assertEquals(Trigger.triggerLongestString(leftMemberTrigger), 0)
    assertEquals(Trigger.triggerLongestString(messageLengthTrigger), 0)
    assertEquals(Trigger.triggerLongestString(commandTrigger), 12)
    assertEquals(Trigger.triggerLongestString(TextTrigger(stringTrigger)), 12)
    assertEquals(Trigger.triggerLongestString(TextTrigger(regexTrigger)), 10)
  }

  test("triggerLongestString should return the expected longest trigger in TextTrigger") {
    val input1: TextTrigger = TextTrigger(
      StringTextTriggerValue("test"),
      StringTextTriggerValue("test 2")
    )

    val input2: TextTrigger = TextTrigger(
      StringTextTriggerValue("longer test"),
      RegexTextTriggerValue("test [0-9]".r, 6)
    )

    assertEquals(Trigger.triggerLongestString(input1), 6)
    assertEquals(Trigger.triggerLongestString(input2), 11)
  }

  test("Regex Json decode/encode should work as expected") {
    val jsonInputs = List(
      """"prendo (il motorino|il coso|la macchina|l'auto)"""",
      """"non sono (uno )?(scemo|stolto)"""",
      """"(e poi[ ,]?[ ]?){2,}"""",
      """"tutti i (suoi )?frutti ti d[aà]"""",
      """"gioco (io )? del gatto e (voi )? del (ratto|topo)"""",
      """"(mica|non) sono come gli altri"""",
      """"(un po'|un attimo) (di|de) esercitazione"""",
      """"amicizie (politiche| d[ie] polizia| d[ie] carabinieri| d[ei] tutt'altr[o]? genere)?"""",
      """"che (cazzo |cazzo di roba )?mi avete dato""""
    )

    for inputString <- jsonInputs
    yield {
      val eitherRegex = decode[Regex](inputString)
      eitherRegex.fold(
        e => fail("failed in parsing the input string as regex", e),
        r => assertEquals(r.asJson.toString, inputString)
      )
    }
  }
}
