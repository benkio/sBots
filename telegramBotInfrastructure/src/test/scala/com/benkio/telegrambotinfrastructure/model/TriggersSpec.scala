package com.benkio.telegrambotinfrastructure.model

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
}
