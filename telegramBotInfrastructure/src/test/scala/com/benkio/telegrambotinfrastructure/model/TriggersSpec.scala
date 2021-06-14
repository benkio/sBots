package com.benkio.telegrambotinfrastructure.model

import org.scalatest._
import matchers.should._
import org.scalatest.wordspec.AnyWordSpec

class TriggersSpec extends AnyWordSpec with Matchers {

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
