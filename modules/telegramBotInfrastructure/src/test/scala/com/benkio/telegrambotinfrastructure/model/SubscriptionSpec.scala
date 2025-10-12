package com.benkio.telegrambotinfrastructure.model

import com.benkio.telegrambotinfrastructure.repository.db.DBSubscriptionData
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
}
