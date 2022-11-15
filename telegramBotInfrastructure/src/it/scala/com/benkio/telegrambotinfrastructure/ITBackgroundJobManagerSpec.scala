package com.benkio.telegrambotinfrastructure

import com.benkio.telegrambotinfrastructure.resources.db.DBSubscriptionData
import java.time.Instant
import cron4s.Cron
import com.benkio.telegrambotinfrastructure.model.Subscription
import java.util.UUID
import cats.effect.IO
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import telegramium.bots.Message
import com.benkio.telegrambotinfrastructure.model.Reply
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.DBFixture
import munit.CatsEffectSuite

class ITBackgroundJobManagerSpec extends CatsEffectSuite with DBFixture {

  implicit val noAction: Action[Reply, IO] = (_: Reply) => (_: Message) => IO.pure(List.empty[Message])

  databaseFixture.test(
    "The creation of the BackgroundJobManager with empty subscriptions should load no subscriptions in memory"
  ) { fixture =>
    for {
      resourceAccess <- fixture.resourceAccessResource
      dbLayer        <- fixture.resourceDBLayer
      backgroundJobManager <- Resource.eval(
        BackgroundJobManager(
          dbSubscription = dbLayer.dbSubscription,
          resourceAccess = resourceAccess,
          youtubeLinkSources = "abar_LinkSources"
        )
      )
    } yield assert(backgroundJobManager.memSubscriptions.size == 0)
  }

  databaseFixture.test(
    "The creation of the BackgroundJobManager with non empty subscriptions should load subscriptions in memory and run the background tasks without errors"
  ) { fixture =>
    val testSubscriptionId = UUID.fromString("9E072CCB-8AF2-457A-9BF6-0F179F4B64D4")

    val testSubscription: Subscription = Subscription(
      id = testSubscriptionId,
      chatId = 0L,
      cron = Cron.unsafeParse("* 0 0,12 1,10 * ?"),
      subscribedAt = Instant.now()
    )

    for {
      resourceAccess <- fixture.resourceAccessResource
      dbLayer        <- fixture.resourceDBLayer
      _              <- Resource.eval(dbLayer.dbSubscription.insertSubscription(DBSubscriptionData(testSubscription)))
      backgroundJobManager <- Resource.eval(
        BackgroundJobManager(
          dbSubscription = dbLayer.dbSubscription,
          resourceAccess = resourceAccess,
          youtubeLinkSources = "abar_LinkSources"
        )
      )
      _ <- Resource.eval(dbLayer.dbSubscription.deleteSubscription(testSubscription.id))
    } yield {
      assert(backgroundJobManager.memSubscriptions.size == 1)
      assert(backgroundJobManager.memSubscriptions.get(testSubscriptionId).isDefined)
      assertIO(backgroundJobManager.memSubscriptions.get(testSubscriptionId).get.get, false)
    }
  }

  databaseFixture.test(
    "BackgroundJobManager.scheduleSubscription should add a subscription to the in memory map and store it in the DB"
  ) { _ =>
    ???
  }

  databaseFixture.test(
    "BackgroundJobManager.cancelSubscription should cancel in memory job, remove the in memory entry and it the db"
  ) { _ =>
    ???
  }

}
