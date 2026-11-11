package com.benkio.integration.integrationmunit.chatcore

import cats.effect.std.Random
import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionId
import com.benkio.chatcore.model.SubscriptionRules
import com.benkio.chatcore.model.SubscriptionType
import com.benkio.chatcore.repository.db.DBMessageReplies
import com.benkio.chatcore.repository.db.DBSubscriptionData
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chatcore.SubscriptionKey
import com.benkio.chattelegramadapter.mocks.ApiMock.given
import com.benkio.chattelegramadapter.mocks.PayloadRecordingApi
import com.benkio.chattelegramadapter.SBot
import com.benkio.chattelegramadapter.TelegramBackgroundJobManager
import com.benkio.integration.DBFixture
import com.benkio.integration.DBFixtureResources
import com.benkio.integration.Logger.given
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import cron4s.*
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.concurrent.SignallingRef
import fs2.Stream
import io.circe.Json
import munit.CatsEffectSuite

import java.time.temporal.ChronoUnit
import java.time.Instant
import java.util.UUID
import scala.concurrent.duration.*

import _root_.log.effect.LogWriter

class ITBackgroundJobManagerSpec extends CatsEffectSuite with DBFixture {

  val sBotConfig                         = SBot.buildSBotConfig(RichardPHJBensonBot.sBotInfo)
  val testSubscriptionId: SubscriptionId = SubscriptionId(UUID.fromString("9E072CCB-8AF2-457A-9BF6-0F179F4B64D4"))
  val sBotInfo                           = sBotConfig.sBotInfo

  val cronScheduler = Cron4sScheduler.systemDefault[IO]

  val testSubscription: Subscription = Subscription(
    id = testSubscriptionId,
    chatId = ChatId.telegram(0L),
    botId = sBotInfo.botId,
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
        TelegramBackgroundJobManager(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotInfo,
          ttl = sBotConfig.messageTimeToLive
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
        TelegramBackgroundJobManager(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      _ <- Resource.eval(dbLayer.dbSubscription.deleteSubscription(testSubscription.id.value))
    } yield {
      assertEquals(backgroundJobManager.getScheduledSubscriptions().size, 1)
      assert(
        backgroundJobManager
          .getScheduledSubscriptions()
          .find { case SubscriptionKey(sId, _, _) =>
            sId == testSubscriptionId
          }
          .isDefined
      )
      assertEquals(
        backgroundJobManager
          .getScheduledSubscriptions()
          .find { case SubscriptionKey(sId, _, _) => sId == testSubscriptionId },
        Some(SubscriptionKey(testSubscriptionId, ChatId.telegram(0L), SubscriptionType.Show))
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
        TelegramBackgroundJobManager(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      _             <- Resource.eval(backgroundJobManager.scheduleSubscription(testSubscription))
      subscriptions <- Resource.eval(dbLayer.dbSubscription.getSubscriptions(sBotInfo.botId))
    } yield {
      assertEquals(backgroundJobManager.getScheduledSubscriptions().size, 1)
      assert(
        backgroundJobManager
          .getScheduledSubscriptions()
          .find { case SubscriptionKey(sId, _, _) =>
            sId == testSubscriptionId
          }
          .isDefined
      )
      assertEquals(
        backgroundJobManager
          .getScheduledSubscriptions()
          .find { case SubscriptionKey(sId, _, _) => sId == testSubscriptionId },
        Some(SubscriptionKey(testSubscriptionId, ChatId.telegram(0L), SubscriptionType.Show))
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
        TelegramBackgroundJobManager(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      streamAndCancel <- Resource.eval(backgroundJobManager.runSubscription(testSubscription))
      (mainStream, cancelSignal) = streamAndCancel
      mainStreamWithCron         = cronScheduler.awakeEvery(testSubscription.cron) >> mainStream
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
      } yield assert(output.length >= 2 && output.length <= 3)
    }
  }

  databaseFixture.test(
    "BackgroundJobManager.cancelSubscription should cancel in memory job, remove the in memory entry and it the db"
  ) { fixture =>
    val result = for {
      dbLayer              <- fixture.resourceDBLayer
      repository           <- fixture.repositoryResource
      backgroundJobManager <- Resource.eval(
        TelegramBackgroundJobManager(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      _ <- Resource.eval(backgroundJobManager.scheduleSubscription(testSubscription))
      _ = println("[ITBackgroundJobManagerSpec] test subscription scheduled")
      inserted_subscriptions <- Resource.eval(dbLayer.dbSubscription.getSubscriptions(sBotInfo.botId))
      _ = println("[ITBackgroundJobManagerSpec] test subscription fetched")
      _ <- Resource.eval(backgroundJobManager.cancelSubscription(testSubscriptionId))
      _ = println("[ITBackgroundJobManagerSpec] test subscription cancelled")
      cancel_subscriptions <- Resource.eval(dbLayer.dbSubscription.getSubscriptions(sBotInfo.botId))
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
        TelegramBackgroundJobManager(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      streamAndCancel <- Resource.eval(backgroundJobManager.runSubscription(testSubscription))
      (mainStream, _) = streamAndCancel
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

  // A cron that never fires during a test, to keep the scheduled subscriptions quiet
  private val quietCron = Cron.unsafeParse("0 0 0 1 1 ?")

  private def quietSubscription(
      chatId: Long,
      subscriptionType: SubscriptionType = SubscriptionType.Show
  ): Subscription =
    testSubscription.copy(
      id = SubscriptionId(UUID.randomUUID()),
      chatId = ChatId.telegram(chatId),
      subscriptionType = subscriptionType,
      cron = quietCron
    )

  private def dbDataOf(subscription: Subscription, botId: SBotId): DBSubscriptionData =
    DBSubscriptionData(subscription).copy(bot_id = botId.value)

  databaseFixture.test(
    "BackgroundJobManager.cancelSubscriptionByType should cancel only the subscriptions of that type for that chat and bot, in memory and in the db"
  ) { fixture =>
    val show         = quietSubscription(0L)
    val calendar     = quietSubscription(0L, SubscriptionType.Calendar)
    val otherChatCal = quietSubscription(1L, SubscriptionType.Calendar)
    val otherBotCal  = dbDataOf(quietSubscription(0L, SubscriptionType.Calendar), SBotId("anotherbot"))
    val result       = for {
      dbLayer              <- fixture.resourceDBLayer
      repository           <- fixture.repositoryResource
      backgroundJobManager <- Resource.eval(
        TelegramBackgroundJobManager(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      _ <- Resource.eval(List(show, calendar, otherChatCal).traverse_(backgroundJobManager.scheduleSubscription))
      _ <- Resource.eval(dbLayer.dbSubscription.insertSubscription(otherBotCal))
      _ <- Resource.eval(backgroundJobManager.cancelSubscriptionByType(ChatId.telegram(0L), SubscriptionType.Calendar))
      stored      <- Resource.eval(dbLayer.dbSubscription.getSubscriptions(sBotInfo.botId))
      storedOther <- Resource.eval(dbLayer.dbSubscription.getSubscriptions(SBotId("anotherbot")))
    } yield {
      assertEquals(
        backgroundJobManager.getScheduledSubscriptions().map(_.subscriptionId).toSet,
        Set(show.id, otherChatCal.id)
      )
      assertEquals(stored.map(_.id).toSet, Set(show.id, otherChatCal.id).map(_.value.toString))
      assertEquals(storedOther, List(otherBotCal))
    }
    result.use_
  }

  databaseFixture.test(
    "BackgroundJobManager.cancelSubscriptionByType should do nothing when the chat has no subscription of that type"
  ) { fixture =>
    val show   = quietSubscription(0L)
    val result = for {
      dbLayer              <- fixture.resourceDBLayer
      repository           <- fixture.repositoryResource
      backgroundJobManager <- Resource.eval(
        TelegramBackgroundJobManager(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      _       <- Resource.eval(backgroundJobManager.scheduleSubscription(show))
      outcome <- Resource.eval(
        backgroundJobManager.cancelSubscriptionByType(ChatId.telegram(0L), SubscriptionType.Calendar).attempt
      )
      stored <- Resource.eval(dbLayer.dbSubscription.getSubscriptions(sBotInfo.botId))
    } yield {
      assertEquals(outcome, Right(()))
      assertEquals(backgroundJobManager.getScheduledSubscriptions().map(_.subscriptionId), List(show.id))
      assertEquals(stored.map(_.id), List(show.id.value.toString))
    }
    result.use_
  }

  databaseFixture.test(
    "BackgroundJobManager.scheduleSubscription should reject the subscription over the per chat limit, counting every type"
  ) { fixture =>
    val limit    = SubscriptionRules.maxSubscriptionsPerChat
    val existing = (1 to limit).toList.map(i =>
      quietSubscription(7L, if i == 1 then SubscriptionType.Calendar else SubscriptionType.Show)
    )
    val result = for {
      dbLayer              <- fixture.resourceDBLayer
      repository           <- fixture.repositoryResource
      backgroundJobManager <- Resource.eval(
        TelegramBackgroundJobManager(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      _ <- Resource.eval(
        existing.init.traverse_(s => dbLayer.dbSubscription.insertSubscription(DBSubscriptionData(s)))
      )
      lastAllowed <- Resource.eval(backgroundJobManager.scheduleSubscription(existing.last).attempt)
      overLimit   <- Resource.eval(backgroundJobManager.scheduleSubscription(quietSubscription(7L)).attempt)
      otherChat   <- Resource.eval(backgroundJobManager.scheduleSubscription(quietSubscription(8L)).attempt)
      stored      <- Resource.eval(dbLayer.dbSubscription.getSubscriptions(sBotInfo.botId, Some(ChatId.telegram(7L))))
    } yield {
      assertEquals(lastAllowed, Right(()), "the subscription number 50 should be allowed")
      assertEquals(
        overLimit.left.map(_.getClass),
        Left(classOf[SubscriptionRules.SubscriptionLimitReached]),
        "the subscription number 51 should be rejected"
      )
      assertEquals(otherChat, Right(()), "the limit is per chat")
      assertEquals(stored.size, limit, "the rejected subscription should not be stored")
    }
    result.use_
  }

  databaseFixture.test(
    "BackgroundJobManager creation should skip the stored subscriptions that cannot be decoded and load the others"
  ) { fixture =>
    val valid   = quietSubscription(0L)
    val invalid = DBSubscriptionData(quietSubscription(0L)).copy(cron = "not a cron")
    val result  = for {
      dbLayer              <- fixture.resourceDBLayer
      repository           <- fixture.repositoryResource
      _                    <- Resource.eval(dbLayer.dbSubscription.insertSubscription(DBSubscriptionData(valid)))
      _                    <- Resource.eval(dbLayer.dbSubscription.insertSubscription(invalid))
      backgroundJobManager <- Resource.eval(
        TelegramBackgroundJobManager(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
    } yield assertEquals(backgroundJobManager.getScheduledSubscriptions().map(_.subscriptionId), List(valid.id))
    result.use_
  }

  private def triggerSubscription(target: Option[String]): Subscription =
    quietSubscription(0L).copy(
      id = SubscriptionId(UUID.randomUUID()),
      subscriptionType = SubscriptionType.Trigger,
      target = target
    )

  private val triggerBundle: ReplyBundleMessage =
    ReplyBundleMessage.textToText("buongiorno")("Buongiorno a te!")

  // A manager over the real DB whose replies are served by the DBLayer, recording what it sends
  private def triggerManager(
      fixture: DBFixtureResources,
      trigger: Subscription
  ): Resource[IO, (BackgroundJobManager[IO], IO[List[(String, Json)]])] =
    for {
      dbLayerBase <- fixture.resourceDBLayer
      dbLayer = dbLayerBase.copy(dbMessageReplies = DBMessageReplies(List(triggerBundle)))
      repository      <- fixture.repositoryResource
      _               <- Resource.eval(dbLayer.dbSubscription.insertSubscription(DBSubscriptionData(trigger)))
      apiAndRecording <- Resource.eval(PayloadRecordingApi())
      (api, recorded) = apiAndRecording
      manager <- Resource.eval(
        TelegramBackgroundJobManager[IO](
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )(using IO.asyncForIO, api, summon[Random[IO]], summon[LogWriter[IO]])
      )
      _ <- Resource.make(IO.unit)(_ => dbLayer.dbSubscription.deleteSubscription(trigger.id.value))
    } yield (manager, recorded)

  databaseFixture.test(
    "A stored trigger subscription is loaded on restart and fires the reply of the matching message bundle"
  ) { fixture =>
    val trigger = triggerSubscription(Some("buongiorno a tutti"))
    val result  = for {
      managerAndCalls <- triggerManager(fixture, trigger)
      (manager, calls) = managerAndCalls
      fired <- Resource.eval(manager.runSubscription(trigger))
      _     <- Resource.eval(fired._1.compile.drain)
      sent  <- Resource.eval(calls)
    } yield {
      assertEquals(manager.getScheduledSubscriptions().map(_.subscriptionType), List(SubscriptionType.Trigger))
      assertEquals(sent.map(_._1), List("sendChatAction", "sendMessage"))
      assertEquals(sent.last._2.hcursor.get[String]("text"), Right("Buongiorno a te!"))
      assert(sent.last._2.hcursor.downField("reply_parameters").failed, sent.last._2.noSpaces)
    }
    result.use_
  }

  databaseFixture.test(
    "A trigger subscription whose text matches no message bundle fires and sends nothing"
  ) { fixture =>
    val trigger = triggerSubscription(Some("arrivederci"))
    val result  = for {
      managerAndCalls <- triggerManager(fixture, trigger)
      (manager, calls) = managerAndCalls
      fired   <- Resource.eval(manager.runSubscription(trigger))
      emitted <- Resource.eval(fired._1.compile.toList)
      sent    <- Resource.eval(calls)
    } yield {
      assertEquals(emitted.size, 1)
      assertEquals(sent, List.empty)
    }
    result.use_
  }
}
