package com.benkio.telegrambotinfrastructure.model

import com.benkio.telegrambotinfrastructure.resources.db.DBSubscriptionData
import munit._

class SubscriptionSpec extends FunSuite {
  test("Subscription.apply should succeed if the input DBSubscriptionData is valid") {
    val input: DBSubscriptionData = DBSubscriptionData(
      id = "232d1421-ad61-4835-b755-f09b07bc34c3",
      chat_id = 0L,
      bot_name = "RichardPHJBensonBot",
      cron = "* 0 0,12 1,10 * ?",
      subscribed_at = "1662126018293"
    )
    val result = Subscription(input)

    assert(result.isRight)
  }
}
