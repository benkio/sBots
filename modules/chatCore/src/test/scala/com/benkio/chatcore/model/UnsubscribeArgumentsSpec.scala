package com.benkio.chatcore.model

import munit.*

import java.util.UUID

class UnsubscribeArgumentsSpec extends FunSuite {

  private val uuid = UUID.fromString("b674cce0-9684-4d31-8cc7-9e2a41ea0878")

  test("empty input means all the subscriptions") {
    assertEquals(UnsubscribeArguments.parse(""), Right(UnsubscribeArguments.All))
    assertEquals(UnsubscribeArguments.parse("   "), Right(UnsubscribeArguments.All))
  }

  test("a uuid means that single subscription, whatever its case") {
    assertEquals(UnsubscribeArguments.parse(uuid.toString), Right(UnsubscribeArguments.ById(SubscriptionId(uuid))))
    assertEquals(
      UnsubscribeArguments.parse(uuid.toString.toUpperCase),
      Right(UnsubscribeArguments.ById(SubscriptionId(uuid)))
    )
  }

  SubscriptionType.values.foreach { subscriptionType =>
    test(s"'${subscriptionType.asString}' means all the subscriptions of that type") {
      assertEquals(
        UnsubscribeArguments.parse(subscriptionType.asString),
        Right(UnsubscribeArguments.ByType(subscriptionType))
      )
      assertEquals(
        UnsubscribeArguments.parse(s" ${subscriptionType.asString.toUpperCase} "),
        Right(UnsubscribeArguments.ByType(subscriptionType))
      )
    }
  }

  test("anything else is rejected with the accepted values in the message") {
    List("foo", "shows", "123", "show trigger").foreach { input =>
      val result = UnsubscribeArguments.parse(input)
      assertEquals(result, Left(UnsubscribeArguments.InvalidUnsubscribeInput(input)))
      assert(result.left.exists(_.getMessage.contains(s"Invalid input '$input'")), result.toString)
      assert(result.left.exists(_.getMessage.contains("show, trigger, calendar")), result.toString)
    }
  }
}
