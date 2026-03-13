package com.benkio.chatcore.model

import munit.CatsEffectSuite

class CommandKeySpec extends CatsEffectSuite {

  test("CommandKey.asString should be unique") {
    val values = CommandKey.values.toList.map(_.asString)
    assertEquals(values.distinct.size, values.size)
  }

  test("CommandKey.fromString should parse canonical command strings") {
    assertEquals(CommandKey.fromString("random"), Some(CommandKey.Random))
    assertEquals(CommandKey.fromString("searchshow"), Some(CommandKey.SearchShow))
    assertEquals(CommandKey.fromString("triggerlist"), Some(CommandKey.TriggerList))
    assertEquals(CommandKey.fromString("triggersearch"), Some(CommandKey.TriggerSearch))
    assertEquals(CommandKey.fromString("instructions"), Some(CommandKey.Instructions))
    assertEquals(CommandKey.fromString("subscribe"), Some(CommandKey.Subscribe))
    assertEquals(CommandKey.fromString("unsubscribe"), Some(CommandKey.Unsubscribe))
    assertEquals(CommandKey.fromString("subscriptions"), Some(CommandKey.Subscriptions))
    assertEquals(CommandKey.fromString("toptwenty"), Some(CommandKey.TopTwenty))
    assertEquals(CommandKey.fromString("timeout"), Some(CommandKey.Timeout))
  }

  test("CommandKey.fromString should normalize leading '/', @botname, casing and whitespace") {
    assertEquals(CommandKey.fromString("/random"), Some(CommandKey.Random))
    assertEquals(CommandKey.fromString("/random@SomeBot"), Some(CommandKey.Random))
    assertEquals(CommandKey.fromString("random@SomeBot"), Some(CommandKey.Random))
    assertEquals(CommandKey.fromString("   /RaNdOm@SomeBot   "), Some(CommandKey.Random))
  }

  test("CommandKey.toStringValue should return the canonical string") {
    assertEquals(CommandKey.toStringValue(CommandKey.Random), "random")
    assertEquals(CommandKey.toStringValue(CommandKey.TriggerSearch), "triggersearch")
  }

  test("CommandKey.commandTriggers should contain the triggers for all known command keys") {
    val expected = CommandKey.values.toList.map(_.trigger)
    assertEquals(CommandKey.commandTriggers, expected)
  }
}
