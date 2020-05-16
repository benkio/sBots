package com.benkio.telegramBotInfrastructure.model

import org.scalatest._

class TriggersSpec extends WordSpec with Matchers {

  "matchValue" should {
    "return true" when {
      "source contains StringTextTriggerValue" in {
        TextTriggerValue.matchValue(StringTextTriggerValue("match"), "source with match in it")
      }
      "source has a match of RegexpTextTriggerValue" in {
        TextTriggerValue.matchValue(RegexTextTriggerValue("m[ae]tch".r), "this is a test with a match")
      }
    }
    "return false" when {
      "source doesn't contains StringTextTriggerValue" in {
        TextTriggerValue.matchValue(StringTextTriggerValue("no match"), "source without match in it")
      }
      "source has not a match of RegexpTextTriggerValue" in {
        TextTriggerValue.matchValue(RegexTextTriggerValue("m[io]tch".r), "this is a test without a match")
      }
    }
  }
}
