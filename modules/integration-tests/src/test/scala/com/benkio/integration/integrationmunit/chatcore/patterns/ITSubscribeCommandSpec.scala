package com.benkio.integration.integrationmunit.chatcore.patterns

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionId
import com.benkio.chatcore.model.SubscriptionRules
import com.benkio.chatcore.model.SubscriptionType
import com.benkio.chatcore.patterns.CommandPatterns.SubscribeUnsubscribeCommand
import com.benkio.chatcore.repository.db.DBMessageReplies
import com.benkio.chatcore.repository.db.DBSubscriptionData
import com.benkio.chattelegramadapter.mocks.ApiMock.given
import com.benkio.chattelegramadapter.SBot
import com.benkio.chattelegramadapter.TelegramBackgroundJobManager
import com.benkio.integration.DBFixture
import com.benkio.integration.DBFixtureResources
import com.benkio.integration.Logger.given
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import cron4s.Cron
import munit.CatsEffectSuite

import java.time.temporal.ChronoUnit
import java.time.Instant
import java.util.UUID

class ITSubscribeCommandSpec extends CatsEffectSuite with DBFixture {

  val sBotConfig                         = SBot.buildSBotConfig(RichardPHJBensonBot.sBotInfo)
  val testSubscriptionId: SubscriptionId = SubscriptionId(UUID.fromString("B674CCE0-9684-4D31-8CC7-9E2A41EA0878"))
  val sBotInfo                           = sBotConfig.sBotInfo
  val chatIdValue                        = 0L
  val chatId                             = ChatId.telegram(chatIdValue)
  val cronValue                          = "0 * * ? * *"
  val cron                               = Cron.unsafeParse(cronValue)

  val testSubscription: Subscription = Subscription(
    id = testSubscriptionId,
    chatId = chatId,
    botId = sBotInfo.botId,
    cron = cron,
    subscribedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS)
  )

  val msg: Message = Message(messageId = 0, date = 0, chatId = chatId, chatType = "private")

  databaseFixture.test(
    "Subscribe Command should add a new subscription"
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
      reply <- Resource.eval(
        SubscribeUnsubscribeCommand
          .subscribeCommandLogic(
            backgroundJobManager = backgroundJobManager,
            dbMessageReplies = dbLayer.dbMessageReplies,
            m = msg.copy(text = Some(s"/subscribe $cronValue")),
            sBotInfo = sBotConfig.sBotInfo,
            ttl = sBotConfig.messageTimeToLive
          )
      )
      subscriptionDatas <- Resource.eval(
        dbLayer.dbSubscription.getSubscriptions(sBotConfig.sBotInfo.botId)
      )
      subscriptions <- Resource.eval(
        subscriptionDatas.traverse(subscriptionData => IO.fromEither(Subscription(subscriptionData)))
      )
    } yield {
      assert(subscriptions.length == 1)
      val testCheck = subscriptions.map { s =>
        val result =
          reply.show.startsWith("Subscription successfully scheduled. Next occurrence of subscription is ") &&
            reply.show.endsWith(s"Refer to this subscription with the ID: `${s.id.value.toString}`")
        if !result then println(s"[ITSubscribeCommandSpec] Failed test with $reply and $s") else ()
        result
      }
      assert(testCheck.forall(_ == true))
    }
    result.use_
  }

  // The only message reply of the bot: the text of a trigger subscription must select it
  private val triggerBundle: ReplyBundleMessage = ReplyBundleMessage.textToText("buongiorno")("Buongiorno a te!")

  private def subscribeResource(fixture: DBFixtureResources, text: String) = for {
    dbLayerBase <- fixture.resourceDBLayer
    dbLayer = dbLayerBase.copy(dbMessageReplies = DBMessageReplies(List(triggerBundle)))
    repository           <- fixture.repositoryResource
    backgroundJobManager <- Resource.eval(
      TelegramBackgroundJobManager(
        repository = repository,
        dbLayer = dbLayer,
        sBotInfo = sBotConfig.sBotInfo,
        ttl = sBotConfig.messageTimeToLive
      )
    )
    reply <- Resource.eval(
      SubscribeUnsubscribeCommand.subscribeCommandLogic(
        backgroundJobManager = backgroundJobManager,
        dbMessageReplies = dbLayer.dbMessageReplies,
        m = msg.copy(text = Some(text)),
        sBotInfo = sBotConfig.sBotInfo,
        ttl = sBotConfig.messageTimeToLive
      )
    )
    stored <- Resource.eval(dbLayer.dbSubscription.getSubscriptions(sBotConfig.sBotInfo.botId))
  } yield (reply, stored, backgroundJobManager.getScheduledSubscriptions())

  databaseFixture.test(
    "Subscribe Command without type should store a show subscription without target"
  ) { fixture =>
    subscribeResource(fixture, s"/subscribe $cronValue").use { case (_, stored, scheduled) =>
      IO {
        assertEquals(stored.map(s => (s.subscription_type, s.target)), List(("show", None)))
        assertEquals(scheduled.map(_.subscriptionType), List(SubscriptionType.Show))
      }
    }
  }

  databaseFixture.test(
    "Subscribe Command with the show type and a target should store the type and the target"
  ) { fixture =>
    subscribeResource(fixture, s"/subscribe $cronValue show pino scotto live").use { case (reply, stored, scheduled) =>
      IO {
        assert(reply.show.startsWith("Subscription successfully scheduled."), reply.show)
        assertEquals(stored.map(s => (s.subscription_type, s.target)), List(("show", Some("pino scotto live"))))
        assertEquals(scheduled.map(_.subscriptionType), List(SubscriptionType.Show))
      }
    }
  }

  databaseFixture.test(
    "Subscribe Command should not store a subscription firing more often than once a minute"
  ) { fixture =>
    subscribeResource(fixture, "/subscribe * * * ? * *").use { case (reply, stored, scheduled) =>
      IO {
        assert(reply.show.contains("The subscription interval is too short"), reply.show)
        assertEquals(stored, List.empty)
        assertEquals(scheduled, List.empty)
      }
    }
  }

  databaseFixture.test(
    "Subscribe Command should not store a subscription with an unsupported type"
  ) { fixture =>
    subscribeResource(fixture, s"/subscribe $cronValue calendar").use { case (reply, stored, scheduled) =>
      IO {
        assert(reply.show.contains("Unsupported subscription type 'calendar'"), reply.show)
        assertEquals(stored, List.empty)
        assertEquals(scheduled, List.empty)
      }
    }
  }

  databaseFixture.test(
    "Subscribe Command should store and schedule a trigger subscription whose text selects a reply"
  ) { fixture =>
    subscribeResource(fixture, s"/subscribe $cronValue trigger buongiorno a tutti").use {
      case (reply, stored, scheduled) =>
        IO {
          assert(reply.show.startsWith("Subscription successfully scheduled."), reply.show)
          assertEquals(stored.map(_.subscription_type), List("trigger"))
          assertEquals(stored.map(_.target), List(Some("buongiorno a tutti")))
          assertEquals(scheduled.map(_.subscriptionType), List(SubscriptionType.Trigger))
        }
    }
  }

  databaseFixture.test(
    "Subscribe Command should reject a trigger subscription whose text selects no reply, storing and scheduling nothing"
  ) { fixture =>
    subscribeResource(fixture, s"/subscribe $cronValue trigger arrivederci").use { case (reply, stored, scheduled) =>
      IO {
        assert(reply.show.contains("The text `arrivederci` triggers no reply of the bot"), reply.show)
        assertEquals(stored, List.empty)
        assertEquals(scheduled, List.empty)
      }
    }
  }

  databaseFixture.test(
    "Subscribe Command should store and schedule a trigger subscription without text: a random reply of the bot"
  ) { fixture =>
    subscribeResource(fixture, s"/subscribe $cronValue trigger").use { case (reply, stored, scheduled) =>
      IO {
        assert(reply.show.startsWith("Subscription successfully scheduled."), reply.show)
        assertEquals(stored.map(_.subscription_type), List("trigger"))
        assertEquals(stored.map(_.target), List(None))
        assertEquals(scheduled.map(_.subscriptionType), List(SubscriptionType.Trigger))
      }
    }
  }

  databaseFixture.test(
    "Subscribe Command should refuse the subscription over the per chat limit"
  ) { fixture =>
    val existing = (1 until SubscriptionRules.maxSubscriptionsPerChat).toList.map(_ =>
      DBSubscriptionData(testSubscription.copy(id = SubscriptionId(UUID.randomUUID())))
    )
    val result = for {
      dbLayer              <- fixture.resourceDBLayer
      repository           <- fixture.repositoryResource
      _                    <- Resource.eval(existing.traverse_(dbLayer.dbSubscription.insertSubscription))
      backgroundJobManager <- Resource.eval(
        TelegramBackgroundJobManager(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotConfig.sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      subscribe = (text: String) =>
        SubscribeUnsubscribeCommand.subscribeCommandLogic(
          backgroundJobManager = backgroundJobManager,
          dbMessageReplies = dbLayer.dbMessageReplies,
          m = msg.copy(text = Some(text)),
          sBotInfo = sBotConfig.sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      lastAllowed <- Resource.eval(subscribe(s"/subscribe $cronValue"))
      overLimit   <- Resource.eval(subscribe(s"/subscribe $cronValue"))
      stored      <- Resource.eval(
        dbLayer.dbSubscription.getSubscriptions(sBotConfig.sBotInfo.botId, Some(ChatId.telegram(chatIdValue)))
      )
    } yield {
      assert(lastAllowed.show.startsWith("Subscription successfully scheduled."), lastAllowed.show)
      assert(overLimit.show.contains("Subscription limit reached"), overLimit.show)
      assertEquals(stored.size, SubscriptionRules.maxSubscriptionsPerChat)
    }
    result.use_
  }
}
