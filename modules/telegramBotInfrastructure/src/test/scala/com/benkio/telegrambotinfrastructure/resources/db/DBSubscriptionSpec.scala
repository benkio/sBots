package com.benkio.telegrambotinfrastructure.resources.db

import com.benkio.telegrambotinfrastructure.model.ChatId
import com.benkio.telegrambotinfrastructure.model.Subscription
import com.benkio.telegrambotinfrastructure.model.SubscriptionId
import little.time.CronSchedule
import munit.*

import java.time.Instant
import java.util.UUID

class DBSubscriptionSpec extends FunSuite {
  test("DBSubscriptionData should be correctly converted from Subscription") {
    val now = Instant.now
    val actual = Subscription(
      id = SubscriptionId(UUID.randomUUID),
      chatId = ChatId(0),
      botName = "botName",
      cron = "5 4 * * *",
      cronScheduler = CronSchedule("0 4 8-14 * *"),
      subscribedAt = now
    )
    val expected = DBSubscriptionData(
      id = actual.id.value.toString,
      chat_id = 0,
      bot_name = "botName",
      cron = actual.cron,
      subscribed_at = now.getEpochSecond.toString
    )
    assertEquals(DBSubscriptionData(actual), expected)
  }
}
