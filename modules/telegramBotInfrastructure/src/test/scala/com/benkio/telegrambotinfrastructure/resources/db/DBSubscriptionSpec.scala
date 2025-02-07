package com.benkio.telegrambotinfrastructure.resources.db

import com.benkio.telegrambotinfrastructure.model.Subscription
import little.time.CronSchedule
import munit.*

import java.time.Instant
import java.util.UUID

class DBSubscriptionSpec extends FunSuite {
  test("DBSubscriptionData should be correctly converted from Subscription") {
    val now = Instant.now
    val actual = Subscription(
      id = UUID.randomUUID,
      chatId = 0,
      botName = "botName",
      cron = "5 4 * * *",
      cronScheduler = CronSchedule("0 4 8-14 * *"),
      subscribedAt = now
    )
    val expected = DBSubscriptionData(
      id = actual.id.toString,
      chat_id = 0,
      bot_name = "botName",
      cron = actual.cron,
      subscribed_at = now.getEpochSecond.toString
    )
    assertEquals(DBSubscriptionData(actual), expected)
  }
}
