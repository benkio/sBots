package com.benkio.integration.integrationmunit.chatcore.patterns

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionId
import com.benkio.chatcore.model.SubscriptionType
import com.benkio.chatcore.patterns.CommandPatterns.SubscribeUnsubscribeCommand
import com.benkio.chatcore.repository.db.DBSubscriptionData
import com.benkio.chattelegramadapter.mocks.ApiMock.given
import com.benkio.chattelegramadapter.SBot
import com.benkio.chattelegramadapter.TelegramBackgroundJobManager
import com.benkio.integration.DBFixture
import com.benkio.integration.Logger.given
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import cron4s.Cron
import munit.CatsEffectSuite

import java.time.temporal.ChronoUnit
import java.time.Instant
import java.util.UUID

class ITUnsubscribeCommandSpec extends CatsEffectSuite with DBFixture {

  val sBotConfig                         = SBot.buildSBotConfig(RichardPHJBensonBot.sBotInfo)
  val testSubscriptionId: SubscriptionId = SubscriptionId(UUID.fromString("B674CCE0-9684-4D31-8CC7-9E2A41EA0878"))
  val botName                            = sBotConfig.sBotInfo.botName
  val botId                              = sBotConfig.sBotInfo.botId
  val chatIdValue                        = 0L
  val chatId                             = ChatId(chatIdValue)

  val testSubscription: Subscription = Subscription(
    id = testSubscriptionId,
    chatId = chatId,
    botId = botId,
    cron = Cron.unsafeParse("* * * ? * *"),
    subscribedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS)
  )

  val msg: Message = Message(messageId = 0, date = 0, chatId = chatId, chatType = "private")

  databaseFixture.test(
    "Unsubscribe Command should fail if the subscription is not found"
  ) { fixture =>
    val subscriptionIdNotFound = "04F08147-DCD7-4F15-9CF8-D7950CB2AD90"
    val result                 = for {
      dbLayer              <- fixture.resourceDBLayer
      repository           <- fixture.repositoryResource
      backgroundJobManager <- Resource.eval(
        TelegramBackgroundJobManager(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotConfig.sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      _     <- Resource.eval(dbLayer.dbSubscription.insertSubscription(DBSubscriptionData(testSubscription)))
      reply <- Resource.eval(
        SubscribeUnsubscribeCommand
          .unsubcribeCommandLogic(
            backgroundJobManager,
            m = msg.copy(text = Some(s"/unsubscribe $subscriptionIdNotFound")),
            sBotInfo = sBotConfig.sBotInfo,
            ttl = sBotConfig.messageTimeToLive
          )
          .attempt
      )
      subscription <- Resource.eval(dbLayer.dbSubscription.getSubscription(testSubscription.id.value.toString))
    } yield {
      assert(subscription.isDefined)
      assertEquals(
        reply.map(_.show),
        Right(
          """An error occurred processing the command: unsubscribe
            | message text: /unsubscribe 04F08147-DCD7-4F15-9CF8-D7950CB2AD90
            | bot: RichardPHJBensonBot
            | error: Subscription Id is not found: 04f08147-dcd7-4f15-9cf8-d7950cb2ad90""".stripMargin
        )
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
        TelegramBackgroundJobManager(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotConfig.sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      _                                    <- Resource.eval(backgroundJobManager.scheduleSubscription(testSubscription))
      reply: Either[Throwable, ReplyValue] <- Resource.eval(
        SubscribeUnsubscribeCommand
          .unsubcribeCommandLogic(
            backgroundJobManager,
            msg.copy(text = Some(s"/unsubscribe ${testSubscriptionId.value.toString}")),
            sBotInfo = sBotConfig.sBotInfo,
            ttl = sBotConfig.messageTimeToLive
          )
          .attempt
      )
      subscription <- Resource.eval(dbLayer.dbSubscription.getSubscription(testSubscription.id.value.toString))
    } yield {
      assert(subscription.isEmpty)
      assertEquals(reply.map(_.show), Right("Subscription successfully cancelled"))
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
        TelegramBackgroundJobManager(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotConfig.sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      _                                    <- Resource.eval(backgroundJobManager.scheduleSubscription(testSubscription))
      reply: Either[Throwable, ReplyValue] <- Resource.eval(
        SubscribeUnsubscribeCommand
          .unsubcribeCommandLogic(
            backgroundJobManager,
            msg.copy(text = Some("/unsubscribe")),
            sBotInfo = sBotConfig.sBotInfo,
            ttl = sBotConfig.messageTimeToLive
          )
          .attempt
      )
      subscription <- Resource.eval(dbLayer.dbSubscription.getSubscription(testSubscription.id.value.toString))
    } yield {
      assert(subscription.isEmpty)
      assertEquals(reply.map(_.show), Right("All Subscriptions for current chat successfully cancelled"))
    }
    result.use_
  }

  databaseFixture.test(
    "Unsubscribe Command with an empty input should remove only the subscriptions of this chat and bot, leaving the ones of the other bots of the chat"
  ) { fixture =>
    val quietCron     = Cron.unsafeParse("0 0 0 1 1 ?")
    val show          = testSubscription.copy(id = SubscriptionId(UUID.randomUUID()), cron = quietCron)
    val calendar      = show.copy(id = SubscriptionId(UUID.randomUUID()), subscriptionType = SubscriptionType.Calendar)
    val otherChatShow = show.copy(id = SubscriptionId(UUID.randomUUID()), chatId = ChatId(1L))
    // another bot in the very same chat
    val otherBotShow = DBSubscriptionData(show.copy(id = SubscriptionId(UUID.randomUUID())))
      .copy(bot_id = "anotherbot")
    val otherBotCalendar = DBSubscriptionData(calendar.copy(id = SubscriptionId(UUID.randomUUID())))
      .copy(bot_id = "anotherbot")
    val result = for {
      dbLayer              <- fixture.resourceDBLayer
      repository           <- fixture.repositoryResource
      backgroundJobManager <- Resource.eval(
        TelegramBackgroundJobManager(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotConfig.sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      _ <- Resource.eval(List(show, calendar, otherChatShow).traverse_(backgroundJobManager.scheduleSubscription))
      // the DB is shared with the other specs: leave it as it was found
      _ <- Resource.make(List(otherBotShow, otherBotCalendar).traverse_(dbLayer.dbSubscription.insertSubscription))(_ =>
        List(otherBotShow, otherBotCalendar).traverse_(s =>
          dbLayer.dbSubscription.deleteSubscription(UUID.fromString(s.id))
        ) *> dbLayer.dbSubscription.deleteSubscriptions(1L, botId)
      )
      reply <- Resource.eval(
        SubscribeUnsubscribeCommand.unsubcribeCommandLogic(
          backgroundJobManager,
          msg.copy(text = Some("/unsubscribe")),
          sBotInfo = sBotConfig.sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      stored      <- Resource.eval(dbLayer.dbSubscription.getSubscriptions(botId))
      storedOther <- Resource.eval(dbLayer.dbSubscription.getSubscriptions(SBotId("anotherbot")))
    } yield {
      assertEquals(reply.show, "All Subscriptions for current chat successfully cancelled")
      assertEquals(backgroundJobManager.getScheduledSubscriptions().map(_.subscriptionId), List(otherChatShow.id))
      assertEquals(stored.map(_.id), List(otherChatShow.id.value.toString), "only this chat and bot are removed")
      assertEquals(
        storedOther.map(_.id).toSet,
        Set(otherBotShow.id, otherBotCalendar.id),
        "the subscriptions of the other bots of the chat are untouched"
      )
    }
    result.use_
  }

  databaseFixture.test(
    "Unsubscribe Command with a type should remove only the subscriptions of that type of this chat and bot, from memory and db"
  ) { fixture =>
    val quietCron    = Cron.unsafeParse("0 0 0 1 1 ?")
    val show         = testSubscription.copy(id = SubscriptionId(UUID.randomUUID()), cron = quietCron)
    val calendar     = show.copy(id = SubscriptionId(UUID.randomUUID()), subscriptionType = SubscriptionType.Calendar)
    val otherChatCal = calendar.copy(id = SubscriptionId(UUID.randomUUID()), chatId = ChatId(1L))
    val otherBotCal  = DBSubscriptionData(calendar.copy(id = SubscriptionId(UUID.randomUUID())))
      .copy(bot_id = "anotherbot")
    val result = for {
      dbLayer              <- fixture.resourceDBLayer
      repository           <- fixture.repositoryResource
      backgroundJobManager <- Resource.eval(
        TelegramBackgroundJobManager(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotConfig.sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      _     <- Resource.eval(List(show, calendar, otherChatCal).traverse_(backgroundJobManager.scheduleSubscription))
      _     <- Resource.eval(dbLayer.dbSubscription.insertSubscription(otherBotCal))
      reply <- Resource.eval(
        SubscribeUnsubscribeCommand.unsubcribeCommandLogic(
          backgroundJobManager,
          msg.copy(text = Some("/unsubscribe calendar")),
          sBotInfo = sBotConfig.sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      stored      <- Resource.eval(dbLayer.dbSubscription.getSubscriptions(botId))
      storedOther <- Resource.eval(dbLayer.dbSubscription.getSubscriptions(SBotId("anotherbot")))
    } yield {
      assertEquals(reply.show, "All calendar subscriptions for current chat successfully cancelled")
      assertEquals(
        backgroundJobManager.getScheduledSubscriptions().map(_.subscriptionId).toSet,
        Set(show.id, otherChatCal.id)
      )
      assertEquals(stored.map(_.id).toSet, Set(show.id, otherChatCal.id).map(_.value.toString))
      assertEquals(storedOther, List(otherBotCal))
    }
    result.use_
  }
}
