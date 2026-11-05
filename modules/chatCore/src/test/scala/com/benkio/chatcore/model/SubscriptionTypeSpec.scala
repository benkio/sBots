package com.benkio.chatcore.model

import munit.*

class SubscriptionTypeSpec extends FunSuite {

  test("asString should be the lowercase name stored in the DB") {
    assertEquals(SubscriptionType.values.toList.map(_.asString), List("show", "trigger", "calendar"))
  }

  test("fromString should be the inverse of asString for every type") {
    SubscriptionType.values.foreach(t => assertEquals(SubscriptionType.fromString(t.asString), Right(t)))
  }

  test("fromString should ignore case and surrounding spaces") {
    assertEquals(SubscriptionType.fromString("  CaLeNdAr "), Right(SubscriptionType.Calendar))
  }

  test("fromString should fail for unknown values") {
    List("", "shows", "unknown", "Show Trigger").foreach(v => assert(SubscriptionType.fromString(v).isLeft, v))
  }
}
