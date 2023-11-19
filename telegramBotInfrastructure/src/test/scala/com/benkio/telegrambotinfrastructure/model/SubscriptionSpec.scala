package com.benkio.telegrambotinfrastructure.model

import com.benkio.telegrambotinfrastructure.resources.db.DBSubscriptionData
import munit.*

class SubscriptionSpec extends FunSuite {

  def buildDBSubscriptionData(cron: String): DBSubscriptionData =
    DBSubscriptionData(
      id = "232d1421-ad61-4835-b755-f09b07bc34c3",
      chat_id = 0L,
      bot_name = "RichardPHJBensonBot",
      cron = cron,
      subscribed_at = "1662126018293"
    )

  test("Subscription.apply should succeed if the input DBSubscriptionData is valid") {
    val inputs: List[DBSubscriptionData] =
      List("5 0 * 8 *", "0 22 * * 1-5", "23 0-20/2 * * *", "5 4 * * sun", "0 0,12 1 */2 *")
        .map(buildDBSubscriptionData)
    val result = inputs.map(Subscription(_))
    result.foreach(r => assert(r.isRight))
  }
}
