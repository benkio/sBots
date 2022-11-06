package com.benkio.telegrambotinfrastructure.resources.db

import com.benkio.telegrambotinfrastructure.model.Subscription
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.DBFixture
import munit.CatsEffectSuite

import cats.effect.IO

class ITDBSubscriptionSpec extends CatsEffectSuite with DBFixture {

  val testSubscription: Subscription = Subscription(
    id = 1,
    chatId = 2,
    cron = "5 4 * * *",
    subscribedAt = "2022-11-06T19:54:46Z"
  )
  databaseFixture.test(
    "DBSubscription: given a subscription, insertSubscription should insert the subscription then getSubscriptions should return the subscription and deleteSubscription should delete it"
  ) { fixture =>
    val resourceAssert = for {
      dbSubscription <- fixture.resourceDBLayer.map(_.dbSubscription)
      _              <- Resource.eval(dbSubscription.insertSubscription(testSubscription))
      subscriptions1 <- Resource.eval(dbSubscription.getSubscriptions())
      _              <- Resource.eval(dbSubscription.deleteSubscription(testSubscription.id))
      subscriptions2 <- Resource.eval(dbSubscription.getSubscriptions())
    } yield subscriptions1.length == 1 && subscriptions1.head == testSubscription && subscriptions2.isEmpty

    resourceAssert.use(IO.pure).assert
  }
}
