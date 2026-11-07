package com.benkio.chatcore.model

import cron4s.Cron
import munit.*

import java.time.LocalDateTime

class SubscriptionRulesSpec extends FunSuite {

  private val from = LocalDateTime.of(2026, 11, 5, 10, 0, 0)

  private def interval(cron: String): Either[Throwable, Unit] =
    SubscriptionRules.validateInterval(Cron.unsafeParse(cron), from)

  test("the limits are the agreed ones") {
    assertEquals(SubscriptionRules.minInterval.toSeconds, 60L)
    assertEquals(SubscriptionRules.maxSubscriptionsPerChat, 50)
    assertEquals(SubscriptionRules.maxTargetLength, 200)
  }

  List(
    "0 * * ? * *",
    "0 */2 * ? * *",
    "0 */5 * ? * *",
    "0 15,30,45 * ? * *",
    "0 0 * ? * *",
    "0 0 9 * * ?",
    "0 30 9 * * ?",
    "0 0 18 ? * MON",
    "0 0 0 12 4 ?",
    "0 0 0 1 1 ?"
  ).foreach { cron =>
    test(s"'$cron' is accepted") {
      assertEquals(interval(cron), Right(()))
    }
  }

  List(
    "* * * ? * *",
    "*/30 * * ? * *",
    "*/20 * * ? * *",
    "0,59 * * ? * *",
    "0,1 * * ? * *"
  ).foreach { cron =>
    test(s"'$cron' is rejected: it fires more often than the minimum") {
      assert(interval(cron).isLeft, cron)
    }
  }

  test("the rejection explains the shortest gap and the minimum") {
    val result = interval("*/30 * * ? * *")
    assertEquals(result.left.map(_.getClass), Left(classOf[SubscriptionRules.IntervalTooShort]))
    assert(result.left.exists(_.getMessage.contains("fires every 30s")), result.toString)
    assert(result.left.exists(_.getMessage.contains("the minimum is 60s")), result.toString)
  }

  test("exactly one minute is accepted") {
    assertEquals(interval("0 * * ? * *"), Right(()))
  }

  test("a gap shorter than the minimum later in the sequence is detected (irregular cron)") {
    // minutes 0 and 59 of each hour: gaps of 59 and 1 minutes, fine; seconds 0 and 59: gaps of 59 seconds, too short
    assertEquals(interval("0 0,59 * ? * *"), Right(()))
    assertEquals(interval("0,59 0 * ? * *").isLeft, true)
  }

  test("target validation accepts none and short values, rejects too long ones") {
    assertEquals(SubscriptionRules.validateTarget(None), Right(()))
    assertEquals(SubscriptionRules.validateTarget(Some("a" * SubscriptionRules.maxTargetLength)), Right(()))
    assert(SubscriptionRules.validateTarget(Some("a" * (SubscriptionRules.maxTargetLength + 1))).isLeft)
  }
}
