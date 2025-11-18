package com.benkio.integration.integrationmunit.telegrambotinfrastructure

import cats.effect.IO
import cats.effect.Resource
import com.benkio.integration.DBFixture
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.model.ChatId
import com.benkio.telegrambotinfrastructure.model.Subscription
import com.benkio.telegrambotinfrastructure.model.SubscriptionId
import com.benkio.telegrambotinfrastructure.repository.db.DBSubscriptionData
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.SubscriptionKey
import cron4s.*
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.concurrent.SignallingRef
import fs2.Stream
import munit.CatsEffectSuite

import java.time.temporal.ChronoUnit
import java.time.Instant
import java.util.UUID
import scala.concurrent.duration.*

class ITBackgroundJobManagerSpec extends CatsEffectSuite with DBFixture {

  val testSubscriptionId: SubscriptionId = SubscriptionId(UUID.fromString("9E072CCB-8AF2-457A-9BF6-0F179F4B64D4"))
  val botId                              = RichardPHJBensonBot.botId

  val cronScheduler = Cron4sScheduler.systemDefault[IO]

  val testSubscription: Subscription = Subscription(
    id = testSubscriptionId,
    chatId = ChatId(0L),
    botId = botId,
    cron = Cron.unsafeParse("* * * ? * *"),
    subscribedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS)
  )

  databaseFixture.test(
    "The creation of the BackgroundJobManager with empty subscriptions should load no subscriptions in memory"
  ) { fixture =>
    val test = for {
      dbLayer <- fixture.resourceDBLayer

      repository           <- fixture.repositoryResource
      backgroundJobManager <- Resource.eval(
        BackgroundJobManager(
          dbSubscription = dbLayer.dbSubscription,
          dbShow = dbLayer.dbShow,
          repository = repository,
          botId = botId
        )
      )
    } yield assert(backgroundJobManager.getScheduledSubscriptions().isEmpty)
    test.use_
  }

  databaseFixture.test(
    "The creation of the BackgroundJobManager with non empty subscriptions should load subscriptions in memory and run the background tasks without errors"
  ) { fixture =>
    val test = for {
      dbLayer    <- fixture.resourceDBLayer
      repository <- fixture.repositoryResource
      _          <- Resource.eval(dbLayer.dbSubscription.insertSubscription(DBSubscriptionData(testSubscription)))
      backgroundJobManager <- Resource.eval(
        BackgroundJobManager(
          dbSubscription = dbLayer.dbSubscription,
          dbShow = dbLayer.dbShow,
          repository = repository,
          botId = botId
        )
      )
      _ <- Resource.eval(dbLayer.dbSubscription.deleteSubscription(testSubscription.id.value))
    } yield {
      assertEquals(backgroundJobManager.getScheduledSubscriptions().size, 1)
      assert(
        backgroundJobManager
          .getScheduledSubscriptions()
          .find { case SubscriptionKey(sId, _) =>
            sId == testSubscriptionId
          }
          .isDefined
      )
      assertEquals(
        backgroundJobManager
          .getScheduledSubscriptions()
          .find { case SubscriptionKey(sId, _) => sId == testSubscriptionId },
        Some(SubscriptionKey(testSubscriptionId, ChatId(0L)))
      )
    }

    test.use_
  }

  databaseFixture.test(
    "BackgroundJobManager.scheduleSubscription should run a subscription, add it to the in memory map and store it in the DB"
  ) { fixture =>
    val test = for {
      dbLayer              <- fixture.resourceDBLayer
      repository           <- fixture.repositoryResource
      backgroundJobManager <- Resource.eval(
        BackgroundJobManager(
          dbSubscription = dbLayer.dbSubscription,
          dbShow = dbLayer.dbShow,
          repository = repository,
          botId = botId
        )
      )
      _             <- Resource.eval(backgroundJobManager.scheduleSubscription(testSubscription))
      subscriptions <- Resource.eval(dbLayer.dbSubscription.getSubscriptions(botId))
    } yield {
      assertEquals(backgroundJobManager.getScheduledSubscriptions().size, 1)
      assert(
        backgroundJobManager
          .getScheduledSubscriptions()
          .find { case SubscriptionKey(sId, _) =>
            sId == testSubscriptionId
          }
          .isDefined
      )
      assertEquals(
        backgroundJobManager
          .getScheduledSubscriptions()
          .find { case SubscriptionKey(sId, _) => sId == testSubscriptionId },
        Some(SubscriptionKey(testSubscriptionId, ChatId(0L)))
      )
      assertEquals(subscriptions.length, 1)
      assertEquals(Subscription(subscriptions.head), Right(testSubscription))
    }
    test.use_
  }

  databaseFixture.test(
    "BackgroundJobManager.runSubscription should return an infinite stream that can be cancelled by the returned signal when is resolved to `true`"
  ) { fixture =>
    val resultStreamResources: Resource[cats.effect.IO, (Stream[IO, Instant], SignallingRef[IO, Boolean])] = for {
      dbLayer              <- fixture.resourceDBLayer
      repository           <- fixture.repositoryResource
      backgroundJobManager <- Resource.eval(
        BackgroundJobManager(
          dbSubscription = dbLayer.dbSubscription,
          dbShow = dbLayer.dbShow,
          repository = repository,
          botId = botId
        )
      )
      (mainStream, cancelSignal) <- Resource.eval(backgroundJobManager.runSubscription(testSubscription))
      mainStreamWithCron = cronScheduler.awakeEvery(testSubscription.cron) >> mainStream
      // if I don't do that the scheduler will keep emitting values and the test will go timeout
      mainStreamWithCronWithCancel = mainStreamWithCron.interruptWhen(cancelSignal)
    } yield (mainStreamWithCronWithCancel, cancelSignal)
    resultStreamResources.use { case (resultStream, cancel) =>
      for {
        resultFiber <- resultStream.take(15).onFinalize(IO(println("Stream interrupted"))).compile.toList.start
        _ = println("[ITBackgroundJobManagerSpec] stream started")
        _ <- IO.sleep(3.seconds)
        _ <- cancel.set(true)
        _ = println("[ITBackgroundJobManagerSpec] stream cancelled")
        output <- resultFiber.joinWithNever
      } yield assertEquals(output.length, 3)
    }
  }

  databaseFixture.test(
    "BackgroundJobManager.cancelSubscription should cancel in memory job, remove the in memory entry and it the db"
  ) { fixture =>
    val result = for {
      dbLayer              <- fixture.resourceDBLayer
      repository           <- fixture.repositoryResource
      backgroundJobManager <- Resource.eval(
        BackgroundJobManager(
          dbSubscription = dbLayer.dbSubscription,
          dbShow = dbLayer.dbShow,
          repository = repository,
          botId = botId
        )
      )
      _ <- Resource.eval(backgroundJobManager.scheduleSubscription(testSubscription))
      _ = println("[ITBackgroundJobManagerSpec] test subscription scheduled")
      inserted_subscriptions <- Resource.eval(dbLayer.dbSubscription.getSubscriptions(botId))
      _ = println("[ITBackgroundJobManagerSpec] test subscription fetched")
      _ <- Resource.eval(backgroundJobManager.cancelSubscription(testSubscriptionId))
      _ = println("[ITBackgroundJobManagerSpec] test subscription cancelled")
      cancel_subscriptions <- Resource.eval(dbLayer.dbSubscription.getSubscriptions(botId))
      _ = println("[ITBackgroundJobManagerSpec] test subscription re-fetched")
    } yield {
      assertEquals(backgroundJobManager.getScheduledSubscriptions().size, 0)
      assertEquals(inserted_subscriptions.length, 1)
      assertEquals(Subscription(inserted_subscriptions.head), Right(testSubscription))
      assert(cancel_subscriptions.isEmpty)
    }
    result.use_
  }

  databaseFixture.test(
    "BackgroundJobManager.runSubscription should return an infinite stream emitting every expected time (second) when scheduled"
  ) { fixture =>
    val resultStreamResources: Resource[cats.effect.IO, Stream[IO, (Instant, Instant)]] = for {
      dbLayer              <- fixture.resourceDBLayer
      repository           <- fixture.repositoryResource
      backgroundJobManager <- Resource.eval(
        BackgroundJobManager(
          dbSubscription = dbLayer.dbSubscription,
          dbShow = dbLayer.dbShow,
          repository = repository,
          botId = botId
        )
      )
      (mainStream, _) <- Resource.eval(backgroundJobManager.runSubscription(testSubscription))
    } yield cronScheduler.awakeEvery(testSubscription.cron) >>
      Stream
        .eval(IO.realTimeInstant)
        .flatMap(testInstant =>
          mainStream
            .map(resultInstant => (resultInstant.truncatedTo(ChronoUnit.SECONDS), testInstant))
        )
    resultStreamResources.use(resultStream =>
      for result <- resultStream.take(3).compile.toList
      yield result.foreach { case (resultI, testI) =>
        assert(
          resultI.isAfter(testI.minusMillis(500).truncatedTo(ChronoUnit.SECONDS)) ||
            resultI.isBefore(testI.plusMillis(500).truncatedTo(ChronoUnit.SECONDS))
        )
      }
    )
  }

}
