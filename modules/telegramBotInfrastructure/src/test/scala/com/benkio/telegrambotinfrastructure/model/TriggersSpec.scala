package com.benkio.telegrambotinfrastructure.model

import cats.implicits.*
import io.circe.parser.decode
import io.circe.syntax.*
import munit.CatsEffectSuite

import scala.concurrent.duration.Duration
import scala.util.matching.Regex

class TriggersSpec extends CatsEffectSuite {

  override def munitIOTimeout = Duration(1, "s")

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

  test("RegexTextTriggerValue.length should be the one expected if specified with `.tr(#)`") {
    val input: List[(RegexTextTriggerValue, Int)] = List(
      "infern[a]+l[e]+[!]*".r.tr(9) -> 9,
      "fi[b]+ri[l]+azioni".r.tr(12) -> 12,
      "fa[s]+[ ]?[b]+inder".r.tr(9) -> 9,
      "infern[a]+l[i]+[!]*".r.tr(9) -> 9,
      "l[i]+[b]+[e]+r[i]+".r.tr(6)  -> 6
    )

    input.foreach { case (regexTextTriggerValue, expected) =>
      assertEquals(regexTextTriggerValue.length, expected)
    }
  }

  test("RegexTextTriggerValue.length should return the expected regex length") {
    val inputExpected: List[(RegexTextTriggerValue, Int)] = List(
      RegexTextTriggerValue("(posto|carico) i video".r)               -> 13,
      RegexTextTriggerValue("(restiamo|teniamo) in contatto".r)       -> 19,
      RegexTextTriggerValue("(attraverso i|nei) commenti".r)          -> 12,
      RegexTextTriggerValue("cocod[eè]".r)                            -> 6,
      RegexTextTriggerValue("\\bmisc\\b".r)                           -> 4,
      RegexTextTriggerValue("\\bm[i]+[a]+[o]+\\b".r)                  -> 4,
      RegexTextTriggerValue("\\bsgrida[tr]".r)                        -> 7,
      RegexTextTriggerValue("\\brimprover".r)                         -> 9,
      RegexTextTriggerValue("(baci )?perugina".r)                     -> 8,
      RegexTextTriggerValue("velit[aà]".r)                            -> 6,
      RegexTextTriggerValue("ho perso (di nuovo )?qualcosa".r)        -> 17,
      RegexTextTriggerValue("tutti (quanti )?mi criticheranno".r)     -> 22,
      RegexTextTriggerValue("ti voglio (tanto )?bene".r)              -> 14,
      RegexTextTriggerValue("vi voglio (tanto )*bene".r)              -> 14,
      RegexTextTriggerValue("pu[oò] capitare".r)                      -> 12,
      RegexTextTriggerValue("dopo (che ho )?mangiato".r)              -> 13,
      RegexTextTriggerValue("\\bops\\b".r)                            -> 3,
      RegexTextTriggerValue("mi sto sentendo (bene|in compagnia)".r)  -> 20,
      RegexTextTriggerValue("(25|venticinque) (milioni|mila euro)".r) -> 10
    )
    inputExpected.foreach { case (regexTextTriggerValue, expected) =>
      assertEquals(
        regexTextTriggerValue.length,
        expected
      )
    }
  }

  test("ShowInstance of Trigger should return the expected string") {
    val textTrigger: Trigger = TextTrigger(
      StringTextTriggerValue("textTriggerValue"),
      RegexTextTriggerValue("regexTriggerValue".r)
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
    val regexTrigger: TextTriggerValue  = RegexTextTriggerValue("test [rR]egex(p)?".r)

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
      RegexTextTriggerValue("test [0-9]".r)
    )

    assertEquals(Trigger.triggerLongestString(input1), 6)
    assertEquals(Trigger.triggerLongestString(input2), 11)
  }

  test("Regex JSON decode/encode should work as expected") {
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

  test("Message Trigger JSON decode/encode should work as expected") {
    val jsonInputs = List(
      """{
        |  "TextTrigger" : {
        |    "triggers" : [
        |      {
        |        "StringTextTriggerValue" : "stringtrigger"
        |      },
        |      {
        |        "RegexTextTriggerValue" : {
        |          "trigger" : "\brege[Xx]?(trigger|test)\b"
        |          "regexLength" : null
        |        }
        |      }
        |    ]
        |  }
        |}""".stripMargin,
      """{
        |  "MessageLengthTrigger" : {
        |    "messageLength" : 12
        |  }
        |}""".stripMargin,
      """{
        |  "NewMemberTrigger" : {
        |    
        |  }
        |}""".stripMargin,
      """{
        |  "LeftMemberTrigger" : {
        |    
        |  }
        |}""".stripMargin
    )

    for inputString <- jsonInputs
    yield {
      val eitherMessageTrigger = decode[MessageTrigger](inputString)
      eitherMessageTrigger.fold(
        e => fail("failed in parsing the input string as message trigger", e),
        ms => assertEquals(ms.asJson.toString, inputString)
      )
    }
  }
}
