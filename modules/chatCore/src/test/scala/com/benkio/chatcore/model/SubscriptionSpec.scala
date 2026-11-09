package com.benkio.chatcore.model

import cats.syntax.all.*
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBSubscriptionData
import munit.*

class SubscriptionSpec extends FunSuite {

  def buildDBSubscriptionData(cron: String): DBSubscriptionData =
    DBSubscriptionData(
      id = "232d1421-ad61-4835-b755-f09b07bc34c3",
      chat_id = 0L,
      bot_id = "rphjb",
      cron = cron,
      subscribed_at = "1662126018293"
    )

  val validDBSubscriptionData: DBSubscriptionData = buildDBSubscriptionData("0 0 0 * * ?")

  test("Subscription.apply should succeed if the input DBSubscriptionData is valid") {
    val inputs: List[DBSubscriptionData] =
      List(
        "* * * ? * *",
        "0 * * ? * *",
        "0 */2 * ? * *",
        "0 */2 * ? * *",
        "0 */3 * ? * *",
        "0 */4 * ? * *",
        "0 */5 * ? * *",
        "0 */10 * ? * *",
        "0 */15 * ? * *",
        "0 */30 * ? * *",
        "0 15,30,45 * ? * *",
        "0 0 * ? * *",
        "0 0 */2 ? * *",
        "0 0 */3 ? * *",
        "0 0 */4 ? * *",
        "0 0 */6 ? * *",
        "0 0 */8 ? * *",
        "0 0 */12 ? * *",
        "0 0 0 * * ?"
      )
        .map(buildDBSubscriptionData)
    val result = inputs.map(Subscription(_))
    assertEquals(result.map(_.isRight), List.fill(result.length)(true))
  }

  test("Subscription.apply from DBSubscriptionData without type information should be a show without target") {
    val result = Subscription(validDBSubscriptionData)
    assertEquals(result.map(_.subscriptionType), Right(SubscriptionType.Show))
    assertEquals(result.map(_.target), Right(None))
  }

  test("Subscription <-> DBSubscriptionData round trip should preserve every subscription type and the target") {
    SubscriptionType.values.foreach { subscriptionType =>
      List(None, Some("some target")).foreach { target =>
        val data = validDBSubscriptionData.copy(subscription_type = subscriptionType.asString, target = target)
        val back = Subscription(data).map(DBSubscriptionData(_))
        assertEquals(back, Right(data))
      }
    }
  }

  test("Subscription.apply should fail when the stored subscription type is unknown") {
    val result = Subscription(validDBSubscriptionData.copy(subscription_type = "unknown"))
    assert(result.isLeft)
  }

  test("Subscription.apply[F] should keep a show subscription without target") {
    val result = Subscription[Either[Throwable, *]](0L, SBotId("rphjb"), "0 0 0 * * ?", SubscriptionType.Show, None)
    assertEquals(result.map(_.subscriptionType), Right(SubscriptionType.Show))
    assertEquals(result.map(_.target), Right(None))
  }

  test("Subscription.apply[F] should keep the given type and target") {
    val result = Subscription[Either[Throwable, *]](
      0L,
      SBotId("rphjb"),
      "0 0 0 * * ?",
      SubscriptionType.Trigger,
      Some("hello")
    )
    assertEquals(result.map(_.subscriptionType), Right(SubscriptionType.Trigger))
    assertEquals(result.map(_.target), Right(Some("hello")))
  }

  test("Subscription.apply[F] should fail for an invalid cron") {
    assert(Subscription[Either[Throwable, *]](0L, SBotId("rphjb"), "not a cron", SubscriptionType.Show, None).isLeft)
  }

  test("Show instance should keep the original rendering") {
    val subscription = Subscription(validDBSubscriptionData).toOption.get
    assertEquals(
      subscription.show,
      s"Subscription Id: `${subscription.id}` - Type: Show - Cron: `${subscription.cron}`"
    )
  }

  test("Show instance should escape the markdown specials of the target") {
    val subscription = Subscription(validDBSubscriptionData.copy(target = Some("a_b *c*"))).toOption.get
    assert(subscription.show.endsWith(" - Target: a\\_b \\*c\\*"), subscription.show)
  }

  test("Show instance should append the target when present") {
    val subscription = Subscription(validDBSubscriptionData.copy(target = Some("some query"))).toOption.get
    assertEquals(
      subscription.show,
      s"Subscription Id: `${subscription.id}` - Type: Show - Cron: `${subscription.cron}` - Target: some query"
    )
  }
}
