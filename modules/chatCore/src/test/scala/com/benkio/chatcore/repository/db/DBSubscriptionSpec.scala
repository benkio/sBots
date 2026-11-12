package com.benkio.chatcore.repository.db

import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionId
import com.benkio.chatcore.model.SubscriptionType
import com.benkio.chatcore.TestIds
import cron4s.*
import munit.*

import java.time.Instant
import java.util.UUID

class DBSubscriptionSpec extends FunSuite {
  test("DBSubscriptionData should be correctly converted from Subscription") {
    val now    = Instant.now
    val actual = Subscription(
      id = SubscriptionId(UUID.randomUUID),
      chatId = TestIds.chatId(0),
      botId = SBotId("botId"),
      cron = Cron.unsafeParse("30 * * * * ?"),
      subscribedAt = now
    )
    val expected = DBSubscriptionData(
      id = actual.id.value.toString,
      platform = "telegram",
      chat_id = "0",
      bot_id = "botId",
      cron = actual.cron.toString,
      subscribed_at = now.getEpochSecond.toString
    )
    assertEquals(DBSubscriptionData(actual), expected)
  }

  test("DBSubscriptionData should default to a show subscription without target") {
    val data = DBSubscriptionData(
      id = "232d1421-ad61-4835-b755-f09b07bc34c3",
      platform = "telegram",
      chat_id = "0",
      bot_id = "botId",
      cron = "30 * * * * ?",
      subscribed_at = "1662126018293"
    )
    assertEquals(data.subscription_type, "show")
    assertEquals(data.target, None)
  }

  test("DBSubscriptionData should carry the type and the target of the Subscription") {
    SubscriptionType.values.foreach { subscriptionType =>
      val subscription = Subscription(
        id = SubscriptionId(UUID.randomUUID),
        chatId = TestIds.chatId(7),
        botId = SBotId("botId"),
        subscriptionType = subscriptionType,
        target = Some("a target"),
        cron = Cron.unsafeParse("30 * * * * ?"),
        subscribedAt = Instant.now
      )
      val data = DBSubscriptionData(subscription)
      assertEquals(data.subscription_type, subscriptionType.asString)
      assertEquals(data.target, Some("a target"))
    }
  }

  test("Subscription should round-trip through DBSubscriptionData on every platform") {
    Platform.values.foreach { platform =>
      val subscription = Subscription(
        id = SubscriptionId(UUID.randomUUID),
        chatId = ChatId(platform, "#chat-1"),
        botId = SBotId("botId"),
        cron = Cron.unsafeParse("30 * * * * ?"),
        subscribedAt = Instant.ofEpochSecond(1)
      )
      val data = DBSubscriptionData(subscription)
      assertEquals(data.platform, platform.id)
      assertEquals(data.chat_id, "#chat-1")
      assertEquals(Subscription(data).map(_.chatId), Right(subscription.chatId))
    }
  }

  test("Subscription should not be built from DBSubscriptionData of an unknown platform") {
    val data = DBSubscriptionData(
      id = "232d1421-ad61-4835-b755-f09b07bc34c3",
      platform = "matrix",
      chat_id = "0",
      bot_id = "botId",
      cron = "30 * * * * ?",
      subscribed_at = "1"
    )
    assert(Subscription(data).isLeft)
  }
}
