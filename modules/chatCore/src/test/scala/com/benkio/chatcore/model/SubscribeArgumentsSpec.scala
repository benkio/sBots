package com.benkio.chatcore.model

import munit.*

class SubscribeArgumentsSpec extends FunSuite {

  private val cron = "0 * * ? * *"

  test("only the cron: a show subscription without target") {
    assertEquals(SubscribeArguments.parse(cron), Right(SubscribeArguments(cron, SubscriptionType.Show, None)))
  }

  test("surrounding and repeated spaces are ignored in the cron") {
    assertEquals(
      SubscribeArguments.parse("  0   *  * ?   * *  "),
      Right(SubscribeArguments(cron, SubscriptionType.Show, None))
    )
  }

  test("cron and the show keyword: a show subscription without target (random show)") {
    assertEquals(SubscribeArguments.parse(s"$cron show"), Right(SubscribeArguments(cron, SubscriptionType.Show, None)))
  }

  test("the show keyword is case insensitive") {
    assertEquals(SubscribeArguments.parse(s"$cron SHOW"), Right(SubscribeArguments(cron, SubscriptionType.Show, None)))
  }

  test("everything after the type is the target, with its inner spaces") {
    assertEquals(
      SubscribeArguments.parse(s"$cron show pino   scotto  live"),
      Right(SubscribeArguments(cron, SubscriptionType.Show, Some("pino   scotto  live")))
    )
  }

  test("the target can be a show id") {
    assertEquals(
      SubscribeArguments.parse(s"$cron show -7kK4eDI6qk"),
      Right(SubscribeArguments(cron, SubscriptionType.Show, Some("-7kK4eDI6qk")))
    )
  }

  test("a cron with fewer than 6 fields is passed through as it is, to be rejected by the cron parser") {
    assertEquals(
      SubscribeArguments.parse("0 * * ?"),
      Right(SubscribeArguments("0 * * ?", SubscriptionType.Show, None))
    )
  }

  List("calendar", "hello", "shows").foreach { token =>
    test(s"'$token' is not a supported type") {
      val result = SubscribeArguments.parse(s"$cron $token something")
      assertEquals(
        result,
        Left(SubscribeArguments.UnsupportedSubscriptionType(token, SubscribeArguments.supportedTypes))
      )
      assert(result.left.exists(_.getMessage.contains(s"Unsupported subscription type '$token'")), result.toString)
      assert(result.left.exists(_.getMessage.contains("Supported types: show, trigger")), result.toString)
    }
  }

  test("a trigger subscription: everything after the type is the text, with its inner spaces") {
    assertEquals(
      SubscribeArguments.parse(s"$cron trigger  buongiorno   a tutti "),
      Right(SubscribeArguments(cron, SubscriptionType.Trigger, Some("buongiorno   a tutti")))
    )
  }

  test("a trigger subscription without text is accepted: a random reply, as a show with no target") {
    List(s"$cron trigger", s"$cron trigger   ").foreach(input =>
      assertEquals(SubscribeArguments.parse(input), Right(SubscribeArguments(cron, SubscriptionType.Trigger, None)))
    )
  }

  test("a trigger text longer than the maximum is rejected") {
    val result = SubscribeArguments.parse(s"$cron trigger ${"a" * (SubscriptionRules.maxTargetLength + 1)}")
    assertEquals(result.left.map(_.getClass), Left(classOf[SubscriptionRules.TargetTooLong]))
  }

  test("a target longer than the maximum is rejected") {
    val result = SubscribeArguments.parse(s"$cron show ${"a" * (SubscriptionRules.maxTargetLength + 1)}")
    assertEquals(result.left.map(_.getClass), Left(classOf[SubscriptionRules.TargetTooLong]))
  }

  test("a target of exactly the maximum length is accepted") {
    val target = "a" * SubscriptionRules.maxTargetLength
    assertEquals(
      SubscribeArguments.parse(s"$cron show $target"),
      Right(SubscribeArguments(cron, SubscriptionType.Show, Some(target)))
    )
  }

  test("NoReplyToTrigger carries the text that triggers nothing and says it in its message") {
    val withText = SubscribeArguments.NoReplyToTrigger(trigger = Some("arrivederci"))
    assertEquals(withText.trigger, Some("arrivederci"))
    assert(withText.getMessage.contains("The text `arrivederci` triggers no reply of the bot"), withText.getMessage)
  }

  test("NoReplyToTrigger without a trigger says the bot has no replies to trigger") {
    val noText = SubscribeArguments.NoReplyToTrigger(trigger = None)
    assertEquals(noText.trigger, None)
    assert(noText.getMessage.contains("The bot has no replies to trigger"), noText.getMessage)
  }
}
