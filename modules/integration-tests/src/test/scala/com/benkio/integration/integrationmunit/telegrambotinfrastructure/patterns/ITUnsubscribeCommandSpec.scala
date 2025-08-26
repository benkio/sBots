package com.benkio.integration.integrationmunit.telegrambotinfrastructure.patterns

import cats.effect.IO
import cats.effect.Resource
import com.benkio.integration.DBFixture
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.model.ChatId
import com.benkio.telegrambotinfrastructure.model.Subscription
import com.benkio.telegrambotinfrastructure.model.SubscriptionId
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.SubscribeUnsubscribeCommand
import com.benkio.telegrambotinfrastructure.repository.db.DBSubscriptionData
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.BackgroundJobManager.SubscriptionIdNotFound
import cron4s.Cron
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message

import java.time.temporal.ChronoUnit
import java.time.Instant
import java.util.UUID

class ITUnsubscribeCommandSpec extends CatsEffectSuite with DBFixture {

  val testSubscriptionId: SubscriptionId = SubscriptionId(UUID.fromString("B674CCE0-9684-4D31-8CC7-9E2A41EA0878"))
  val botName                            = RichardPHJBensonBot.botName
  val chatIdValue                        = 0L
  val chatId                             = ChatId(chatIdValue)

  val testSubscription: Subscription = Subscription(
    id = testSubscriptionId,
    chatId = chatId,
    botName = botName,
    cron = Cron.unsafeParse("* * * ? * *"),
    subscribedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS)
  )

  val msg: Message = Message(
    messageId = 0,
    date = 0,
    chat = Chat(id = chatIdValue, `type` = "private")
  )

  databaseFixture.test(
    "Unsubscribe Command should fail if the subscription is not found"
  ) { fixture =>
    val subscriptionIdNotFound = "04F08147-DCD7-4F15-9CF8-D7950CB2AD90"
    val result                 = for {
      dbLayer              <- fixture.resourceDBLayer
      repository           <- fixture.repositoryResource
      backgroundJobManager <- Resource.eval(
        BackgroundJobManager(
          dbSubscription = dbLayer.dbSubscription,
          dbShow = dbLayer.dbShow,
          repository = repository,
          botName = botName
        )
      )
      _     <- Resource.eval(dbLayer.dbSubscription.insertSubscription(DBSubscriptionData(testSubscription)))
      reply <- Resource.eval(
        SubscribeUnsubscribeCommand
          .unsubcribeCommandLogic[IO](
            subscriptionIdNotFound,
            backgroundJobManager,
            msg
          )
          .attempt
      )
      subscription <- Resource.eval(dbLayer.dbSubscription.getSubscription(testSubscription.id.value.toString))
    } yield {
      assert(subscription.isDefined)
      assertEquals(
        reply,
        Left(SubscriptionIdNotFound(subscriptionId = SubscriptionId(UUID.fromString(subscriptionIdNotFound))))
      )
    }
    result.use_
  }

  databaseFixture.test(
    "Unsubscribe Command should remove the input existing subscription"
  ) { fixture =>
    val result = for {
      dbLayer              <- fixture.resourceDBLayer
      repository           <- fixture.repositoryResource
      backgroundJobManager <- Resource.eval(
        BackgroundJobManager(
          dbSubscription = dbLayer.dbSubscription,
          dbShow = dbLayer.dbShow,
          repository = repository,
          botName = botName
        )
      )
      _     <- Resource.eval(backgroundJobManager.scheduleSubscription(testSubscription))
      reply <- Resource.eval(
        SubscribeUnsubscribeCommand
          .unsubcribeCommandLogic[IO](
            testSubscription.id.value.toString,
            backgroundJobManager,
            msg
          )
          .attempt
      )
      subscription <- Resource.eval(dbLayer.dbSubscription.getSubscription(testSubscription.id.value.toString))
    } yield {
      assert(subscription.isEmpty)
      assertEquals(reply, Right("Subscription successfully cancelled"))
    }
    result.use_
  }

  databaseFixture.test(
    "Unsubscribe Command should remove all chat subscription if input is empty"
  ) { fixture =>
    val result = for {
      dbLayer              <- fixture.resourceDBLayer
      repository           <- fixture.repositoryResource
      backgroundJobManager <- Resource.eval(
        BackgroundJobManager(
          dbSubscription = dbLayer.dbSubscription,
          dbShow = dbLayer.dbShow,
          repository = repository,
          botName = botName
        )
      )
      _     <- Resource.eval(backgroundJobManager.scheduleSubscription(testSubscription))
      reply <- Resource.eval(
        SubscribeUnsubscribeCommand
          .unsubcribeCommandLogic[IO](
            "",
            backgroundJobManager,
            msg
          )
          .attempt
      )
      subscription <- Resource.eval(dbLayer.dbSubscription.getSubscription(testSubscription.id.value.toString))
    } yield {
      assert(subscription.isEmpty)
      assertEquals(reply, Right("All Subscriptions for current chat successfully cancelled"))
    }
    result.use_
  }
}
