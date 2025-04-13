package com.benkio.integration.integrationmunit.telegrambotinfrastructure

import cats.effect.kernel.Outcome
import cats.effect.IO
import cats.effect.Resource
import com.benkio.integration.DBFixture
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.model.ChatId
import com.benkio.telegrambotinfrastructure.model.Subscription
import com.benkio.telegrambotinfrastructure.model.SubscriptionId
import com.benkio.telegrambotinfrastructure.resources.db.DBSubscriptionData
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.SubscriptionKey
import munit.CatsEffectSuite
import cron4s.*

import java.time.Instant
import java.util.UUID

class ITBackgroundJobManagerSpec extends CatsEffectSuite with DBFixture {

  val testSubscriptionId: SubscriptionId = SubscriptionId(UUID.fromString("9E072CCB-8AF2-457A-9BF6-0F179F4B64D4"))
  val botName                            = "botname"

  val testSubscription: Subscription = Subscription(
    id = testSubscriptionId,
    chatId = ChatId(0L),
    botName = botName,
    cron = Cron.unsafeParse( "0 4 8-14 * *"),
    subscribedAt = Instant.now()
  )

  databaseFixture.test(
    "The creation of the BackgroundJobManager with empty subscriptions should load no subscriptions in memory"
  ) { fixture =>
    for {
      dbLayer <- fixture.resourceDBLayer

      resourceAccess <- fixture.resourceAccessResource
      backgroundJobManager <- Resource.eval(
        BackgroundJobManager(
          dbSubscription = dbLayer.dbSubscription,
          dbShow = dbLayer.dbShow,
          resourceAccess = resourceAccess,
          botName = botName
        )
      )
    } yield assert(backgroundJobManager.getScheduledSubscriptions().isEmpty)
  }

  databaseFixture.test(
    "The creation of the BackgroundJobManager with non empty subscriptions should load subscriptions in memory and run the background tasks without errors"
  ) { fixture =>
    for {
      dbLayer        <- fixture.resourceDBLayer
      resourceAccess <- fixture.resourceAccessResource
      _              <- Resource.eval(dbLayer.dbSubscription.insertSubscription(DBSubscriptionData(testSubscription)))
      backgroundJobManager <- Resource.eval(
        BackgroundJobManager(
          dbSubscription = dbLayer.dbSubscription,
          dbShow = dbLayer.dbShow,
          resourceAccess = resourceAccess,
          botName = botName
        )
      )
      _ <- Resource.eval(dbLayer.dbSubscription.deleteSubscription(testSubscription.id.value))
    } yield {
      assert(backgroundJobManager.getScheduledSubscriptions().size == 1)
      assert(backgroundJobManager.getScheduledSubscriptions().find { case (SubscriptionKey(sId, _), _) =>
        sId == testSubscriptionId
      }.isDefined)
      assertIO(
        backgroundJobManager.getScheduledSubscriptions()
          .find { case (SubscriptionKey(sId, _), _) => sId == testSubscriptionId }
          .get
          ._2
          .join,
        Outcome.succeeded[IO, Throwable, Unit](IO(()))
      )
    }
  }

  databaseFixture.test(
    "BackgroundJobManager.scheduleSubscription should run a subscription, add it to the in memory map and store it in the DB"
  ) { fixture =>
    for {
      dbLayer        <- fixture.resourceDBLayer
      resourceAccess <- fixture.resourceAccessResource
      backgroundJobManager <- Resource.eval(
        BackgroundJobManager(
          dbSubscription = dbLayer.dbSubscription,
          dbShow = dbLayer.dbShow,
          resourceAccess = resourceAccess,
          botName = botName
        )
      )
      _             <- Resource.eval(backgroundJobManager.scheduleSubscription(testSubscription))
      subscriptions <- Resource.eval(dbLayer.dbSubscription.getSubscriptions(botName))
      _ <- Resource.eval(
        assertIO(
          backgroundJobManager.getScheduledSubscriptions()
            .find { case (SubscriptionKey(sId, _), _) => sId == testSubscriptionId }
            .get
            ._2
            .join,
          Outcome.succeeded[IO, Throwable, Unit](IO(()))
        )
      )
    } yield {
      assert(backgroundJobManager.getScheduledSubscriptions().size == 1)
      assert(backgroundJobManager.getScheduledSubscriptions().find { case (SubscriptionKey(sId, _), _) =>
        sId == testSubscriptionId
      }.isDefined)
      assert(subscriptions.length == 1)
      assert(Subscription(subscriptions.head) == testSubscription)
    }
  }

  databaseFixture.test(
    "BackgroundJobManager.cancelSubscription should cancel in memory job, remove the in memory entry and it the db"
  ) { fixture =>
    for {
      dbLayer        <- fixture.resourceDBLayer
      resourceAccess <- fixture.resourceAccessResource
      backgroundJobManager <- Resource.eval(
        BackgroundJobManager(
          dbSubscription = dbLayer.dbSubscription,
          dbShow = dbLayer.dbShow,
          resourceAccess = resourceAccess,
          botName = botName
        )
      )
      _                      <- Resource.eval(backgroundJobManager.scheduleSubscription(testSubscription))
      inserted_subscriptions <- Resource.eval(dbLayer.dbSubscription.getSubscriptions(botName))
      _                      <- Resource.eval(backgroundJobManager.cancelSubscription(testSubscriptionId))
      cancel_subscriptions   <- Resource.eval(dbLayer.dbSubscription.getSubscriptions(botName))
    } yield {
      assert(backgroundJobManager.getScheduledSubscriptions().size == 0)
      assert(inserted_subscriptions.length == 1)
      assert(Subscription(inserted_subscriptions.head) == testSubscription)
      assert(cancel_subscriptions.isEmpty)
    }
  }

}
