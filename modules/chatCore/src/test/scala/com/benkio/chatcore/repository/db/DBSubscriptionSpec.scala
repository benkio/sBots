package com.benkio.chatcore.repository.db

import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionId
import cron4s.*
import munit.*

import java.time.Instant
import java.util.UUID

class DBSubscriptionSpec extends FunSuite {
  test("DBSubscriptionData should be correctly converted from Subscription") {
    val now    = Instant.now
    val actual = Subscription(
      id = SubscriptionId(UUID.randomUUID),
      chatId = ChatId(0),
      botId = SBotId("botId"),
      cron = Cron.unsafeParse("30 * * * * ?"),
      subscribedAt = now
    )
    val expected = DBSubscriptionData(
      id = actual.id.value.toString,
      chat_id = 0,
      bot_id = "botId",
      cron = actual.cron.toString,
      subscribed_at = now.getEpochSecond.toString
    )
    assertEquals(DBSubscriptionData(actual), expected)
  }
}
