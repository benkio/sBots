package com.benkio.telegrambotinfrastructure.resources.db

import java.util.UUID
import com.benkio.telegrambotinfrastructure.resources.db.DBSubscriptionData
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.DBFixture
import munit.CatsEffectSuite

import cats.effect.IO

class ITDBSubscriptionSpec extends CatsEffectSuite with DBFixture {

  val botName = "botname"

  val testSubscription: DBSubscriptionData = DBSubscriptionData(
    id = "83beabe1-cad9-4845-a838-65bbed34bc46",
    chat_id = 2,
    bot_name = botName,
    cron = "0 0 0 12 4 *",
    subscribed_at = "2022-11-06T19:54:46Z"
  )
  databaseFixture.test(
    "DBSubscription: given a subscription, insertSubscription should insert the subscription then getSubscriptions should return the subscription and deleteSubscription should delete it"
  ) { fixture =>
    val resourceAssert = for {
      dbSubscription <- fixture.resourceDBLayer.map(_.dbSubscription)
      _              <- Resource.eval(dbSubscription.insertSubscription(testSubscription))
      subscriptions1 <- Resource.eval(dbSubscription.getSubscriptionsByBotName(botName))
      _              <- Resource.eval(dbSubscription.deleteSubscription(UUID.fromString(testSubscription.id)))
      subscriptions2 <- Resource.eval(dbSubscription.getSubscriptionsByBotName(botName))
    } yield (subscriptions1, subscriptions2)

    resourceAssert.use { case (preSubscriptions, postSubscriptions) =>
      IO {
        assert(preSubscriptions.length == 1)
        assert(preSubscriptions.head == testSubscription)
        assert(postSubscriptions.isEmpty)
      }
    }
  }
}
