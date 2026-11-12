package com.benkio.integration.integrationmunit.chatcore.repository.db

import cats.effect.IO
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.SubscriptionType
import com.benkio.chatcore.repository.db.DBSubscription
import com.benkio.chatcore.repository.db.DBSubscriptionData
import com.benkio.chatcore.TestIds
import com.benkio.integration.DBFixture
import doobie.munit.analysisspec.IOChecker
import doobie.Transactor
import munit.CatsEffectSuite

import java.sql.DriverManager
import java.util.UUID

class ITDBSubscriptionSpec extends CatsEffectSuite with DBFixture with IOChecker {

  val botId                      = SBotId("botid")
  val testSubscriptionId: String = "83beabe1-cad9-4845-a838-65bbed34bc46"

  val testSubscription: DBSubscriptionData = DBSubscriptionData(
    id = testSubscriptionId,
    platform = "telegram",
    chat_id = "2",
    bot_id = botId.value,
    cron = "0 0 0 12 4 *",
    subscribed_at = "2022-11-06T19:54:46Z"
  )

  override def transactor: doobie.Transactor[cats.effect.IO] = {
    Class.forName("org.sqlite.JDBC")
    val conn = DriverManager.getConnection(DBFixture.dbUrl)
    DBFixture.runMigrations(DBFixture.dbUrl, DBFixture.migrationTable, DBFixture.migrationPath)
    val transactor = Transactor.fromConnection[IO](conn, None)
    transactor
  }

  test(
    "DBSubscription queries should check".ignore
  ) {
    check(DBSubscription.getSubscriptionsQuery(botId))
    check(DBSubscription.insertSubscriptionQuery(testSubscription))
    check(DBSubscription.deleteSubscriptionQuery(testSubscriptionId))
    check(DBSubscription.getSubscriptionQuery(testSubscriptionId))
    check(DBSubscription.getRandomSubscriptionQuery())
    check(DBSubscription.deleteSubscriptionsQuery(TestIds.chatId(2L), botId))
    check(DBSubscription.deleteSubscriptionsByTypeQuery(TestIds.chatId(2L), botId, SubscriptionType.Calendar))
  }

  databaseFixture.test(
    "DBSubscription: given a subscription, insertSubscription should insert the subscription then getSubscriptions should return the subscription and deleteSubscription should delete it"
  ) { fixture =>
    val resourceAssert = for {
      dbSubscription <- fixture.resourceDBLayer.map(_.dbSubscription)
      _              <- Resource.eval(dbSubscription.insertSubscription(testSubscription))
      subscriptions1 <- Resource.eval(dbSubscription.getSubscriptions(botId))
      _              <- Resource.eval(dbSubscription.deleteSubscription(UUID.fromString(testSubscription.id)))
      subscriptions2 <- Resource.eval(dbSubscription.getSubscriptions(botId))
    } yield (subscriptions1, subscriptions2)

    resourceAssert.use { case (preSubscriptions, postSubscriptions) =>
      IO {
        assert(preSubscriptions.length == 1)
        assert(preSubscriptions.head == testSubscription)
        assert(postSubscriptions.isEmpty)
      }
    }
  }

  databaseFixture.test(
    "DBSubscription.getRandomSubscription should return a subscription when called"
  ) { fixture =>
    val resourceAssert = for {
      dbSubscription <- fixture.resourceDBLayer.map(_.dbSubscription)
      _              <- Resource.eval(dbSubscription.insertSubscription(testSubscription))
      result         <- Resource.eval(dbSubscription.getRandomSubscription())
    } yield assertEquals(result, Some(testSubscription))
    resourceAssert.use_
  }

  private def subscriptionOf(
      id: String,
      chatId: Long,
      botIdValue: String = botId.value,
      subscriptionType: SubscriptionType = SubscriptionType.Show,
      target: Option[String] = None
  ): DBSubscriptionData =
    testSubscription.copy(
      id = id,
      platform = "telegram",
      chat_id = chatId.toString,
      bot_id = botIdValue,
      subscription_type = subscriptionType.asString,
      target = target
    )

  databaseFixture.test(
    "DBSubscription: type and target should be stored and read back for every type"
  ) { fixture =>
    val subscriptions = SubscriptionType.values.toList.zipWithIndex.map { case (subscriptionType, index) =>
      subscriptionOf(
        id = UUID.randomUUID().toString,
        chatId = 100L + index,
        subscriptionType = subscriptionType,
        target = Option.when(subscriptionType != SubscriptionType.Calendar)(s"target $index")
      )
    }
    val resourceAssert = for {
      dbSubscription <- fixture.resourceDBLayer.map(_.dbSubscription)
      _              <- Resource.eval(subscriptions.traverse_(dbSubscription.insertSubscription))
      results        <- Resource.eval(subscriptions.traverse(s => dbSubscription.getSubscription(s.id)))
    } yield results
    resourceAssert.use(results => IO(assertEquals(results, subscriptions.map(Some(_)))))
  }

  databaseFixture.test(
    "DBSubscription: a row inserted without the new columns should read back as a show without target"
  ) { fixture =>
    val id = UUID.randomUUID().toString
    IO {
      fixture.connection
        .createStatement()
        .executeUpdate(
          s"INSERT INTO subscription (subscription_id, platform, chat_id, bot_id, cron, subscribed_at) VALUES ('$id', 'telegram', '3', '${botId.value}', '0 0 0 12 4 *', '1')"
        )
    } *> fixture.resourceDBLayer
      .map(_.dbSubscription)
      .use(_.getSubscription(id))
      .map(result =>
        assertEquals(
          result.map(r => (r.subscription_type, r.target)),
          Some(("show", None))
        )
      )
  }

  databaseFixture.test(
    "DBSubscription: a second calendar subscription for the same chat and bot should be rejected"
  ) { fixture =>
    val first =
      subscriptionOf(id = UUID.randomUUID().toString, chatId = 11L, subscriptionType = SubscriptionType.Calendar)
    val second =
      subscriptionOf(id = UUID.randomUUID().toString, chatId = 11L, subscriptionType = SubscriptionType.Calendar)
    val resourceAssert = for {
      dbSubscription <- fixture.resourceDBLayer.map(_.dbSubscription)
      _              <- Resource.eval(dbSubscription.insertSubscription(first))
      rejected       <- Resource.eval(dbSubscription.insertSubscription(second).attempt)
      stored         <- Resource.eval(dbSubscription.getSubscriptions(botId, Some(TestIds.chatId(11L))))
    } yield (rejected, stored)
    resourceAssert.use { case (rejected, stored) =>
      IO {
        assert(rejected.isLeft, "the second calendar subscription should have been rejected")
        assertEquals(stored, List(first))
      }
    }
  }

  databaseFixture.test(
    "DBSubscription: calendar subscriptions of other chats or other bots, and non calendar subscriptions, should be allowed"
  ) { fixture =>
    val calendar =
      subscriptionOf(id = UUID.randomUUID().toString, chatId = 12L, subscriptionType = SubscriptionType.Calendar)
    val otherChat =
      subscriptionOf(id = UUID.randomUUID().toString, chatId = 13L, subscriptionType = SubscriptionType.Calendar)
    val otherBot = subscriptionOf(
      id = UUID.randomUUID().toString,
      chatId = 12L,
      botIdValue = "anotherbot",
      subscriptionType = SubscriptionType.Calendar
    )
    val shows    = List.fill(3)(subscriptionOf(id = UUID.randomUUID().toString, chatId = 12L))
    val triggers = List.fill(2)(
      subscriptionOf(
        id = UUID.randomUUID().toString,
        chatId = 12L,
        subscriptionType = SubscriptionType.Trigger,
        target = Some("hello")
      )
    )
    val all            = calendar :: otherChat :: otherBot :: (shows ++ triggers)
    val resourceAssert = for {
      dbSubscription <- fixture.resourceDBLayer.map(_.dbSubscription)
      results        <- Resource.eval(all.traverse(s => dbSubscription.insertSubscription(s).attempt))
    } yield results
    resourceAssert.use(results => IO(assert(results.forall(_.isRight), results.toString)))
  }

  databaseFixture.test(
    "DBSubscription: the calendar subscription can be created again once the previous one is deleted"
  ) { fixture =>
    val first =
      subscriptionOf(id = UUID.randomUUID().toString, chatId = 14L, subscriptionType = SubscriptionType.Calendar)
    val second =
      subscriptionOf(id = UUID.randomUUID().toString, chatId = 14L, subscriptionType = SubscriptionType.Calendar)
    val resourceAssert = for {
      dbSubscription <- fixture.resourceDBLayer.map(_.dbSubscription)
      _              <- Resource.eval(dbSubscription.insertSubscription(first))
      _              <- Resource.eval(dbSubscription.deleteSubscription(UUID.fromString(first.id)))
      result         <- Resource.eval(dbSubscription.insertSubscription(second).attempt)
    } yield result
    resourceAssert.use(result => IO(assert(result.isRight, result.toString)))
  }

  databaseFixture.test(
    "DBSubscription: an unknown subscription type should be rejected by the database"
  ) { fixture =>
    val invalid = testSubscription.copy(
      id = UUID.randomUUID().toString,
      platform = "telegram",
      chat_id = "15",
      subscription_type = "weird"
    )
    fixture.resourceDBLayer
      .map(_.dbSubscription)
      .use(_.insertSubscription(invalid).attempt)
      .map(result => assert(result.isLeft, "an unknown type should violate the CHECK constraint"))
  }

  databaseFixture.test(
    "DBSubscription.deleteSubscriptionsByType should delete only the subscriptions of that type for that chat and bot"
  ) { fixture =>
    val anotherBot = "anotherbot"
    val calendar   =
      subscriptionOf(id = UUID.randomUUID().toString, chatId = 16L, subscriptionType = SubscriptionType.Calendar)
    val show    = subscriptionOf(id = UUID.randomUUID().toString, chatId = 16L)
    val trigger = subscriptionOf(
      id = UUID.randomUUID().toString,
      chatId = 16L,
      subscriptionType = SubscriptionType.Trigger,
      target = Some("t")
    )
    val otherChatCal =
      subscriptionOf(id = UUID.randomUUID().toString, chatId = 17L, subscriptionType = SubscriptionType.Calendar)
    val otherBotCal = subscriptionOf(
      id = UUID.randomUUID().toString,
      chatId = 16L,
      botIdValue = anotherBot,
      subscriptionType = SubscriptionType.Calendar
    )
    val resourceAssert = for {
      dbSubscription <- fixture.resourceDBLayer.map(_.dbSubscription)
      _              <- Resource.eval(
        List(calendar, show, trigger, otherChatCal, otherBotCal).traverse_(dbSubscription.insertSubscription)
      )
      _ <- Resource.eval(
        dbSubscription.deleteSubscriptionsByType(TestIds.chatId(16L), botId, SubscriptionType.Calendar)
      )
      chat16     <- Resource.eval(dbSubscription.getSubscriptions(botId, Some(TestIds.chatId(16L))))
      chat17     <- Resource.eval(dbSubscription.getSubscriptions(botId, Some(TestIds.chatId(17L))))
      otherBot16 <- Resource.eval(dbSubscription.getSubscriptions(SBotId(anotherBot), Some(TestIds.chatId(16L))))
    } yield (chat16, chat17, otherBot16)
    resourceAssert.use { case (chat16, chat17, otherBot16) =>
      IO {
        assertEquals(chat16.map(_.id).toSet, Set(show.id, trigger.id))
        assertEquals(chat17, List(otherChatCal))
        assertEquals(otherBot16, List(otherBotCal))
      }
    }
  }

  databaseFixture.test(
    "DBSubscription: the same chat id on two platforms should be two different chats"
  ) { fixture =>
    val telegramCalendar =
      subscriptionOf(id = UUID.randomUUID().toString, chatId = 31L, subscriptionType = SubscriptionType.Calendar)
    val ircCalendar    = telegramCalendar.copy(id = UUID.randomUUID().toString, platform = Platform.Irc.id)
    val resourceAssert = for {
      dbSubscription <- fixture.resourceDBLayer.map(_.dbSubscription)
      // one calendar per chat and bot: the same chat id on another platform is another chat
      _              <- Resource.eval(dbSubscription.insertSubscription(telegramCalendar))
      _              <- Resource.eval(dbSubscription.insertSubscription(ircCalendar))
      telegramStored <- Resource.eval(dbSubscription.getSubscriptions(botId, Some(TestIds.chatId(31L))))
      ircStored      <- Resource.eval(dbSubscription.getSubscriptions(botId, Some(ChatId(Platform.Irc, "31"))))
      _              <- Resource.eval(dbSubscription.deleteSubscriptions(TestIds.chatId(31L), botId))
      ircAfter       <- Resource.eval(dbSubscription.getSubscriptions(botId, Some(ChatId(Platform.Irc, "31"))))
    } yield (telegramStored, ircStored, ircAfter)
    resourceAssert.use { case (telegramStored, ircStored, ircAfter) =>
      IO {
        assertEquals(telegramStored, List(telegramCalendar))
        assertEquals(ircStored, List(ircCalendar))
        assertEquals(ircAfter, List(ircCalendar))
      }
    }
  }

  databaseFixture.test("DBSubscription: the subscriptions of a bot can be listed by platform") { fixture =>
    val telegram       = subscriptionOf(id = UUID.randomUUID().toString, chatId = 41L)
    val irc            = telegram.copy(id = UUID.randomUUID().toString, platform = Platform.Irc.id)
    val discord        = telegram.copy(id = UUID.randomUUID().toString, platform = Platform.Discord.id)
    val ours           = Set(telegram.id, irc.id, discord.id)
    val resourceAssert = for {
      dbSubscription <- fixture.resourceDBLayer.map(_.dbSubscription)
      _              <- Resource.eval(List(telegram, irc, discord).traverse_(dbSubscription.insertSubscription))
      onIrc          <- Resource.eval(dbSubscription.getSubscriptions(botId, platform = Some(Platform.Irc)))
      onTelegram     <- Resource.eval(dbSubscription.getSubscriptions(botId, platform = Some(Platform.Telegram)))
      onSlack        <- Resource.eval(dbSubscription.getSubscriptions(botId, platform = Some(Platform.Slack)))
      onAny          <- Resource.eval(dbSubscription.getSubscriptions(botId))
      ofChat         <- Resource.eval(
        dbSubscription.getSubscriptions(botId, Some(ChatId(Platform.Irc, "41")), Some(Platform.Irc))
      )
      ofChatOtherPlatform <- Resource.eval(
        dbSubscription.getSubscriptions(botId, Some(ChatId(Platform.Irc, "41")), Some(Platform.Telegram))
      )
    } yield (onIrc, onTelegram, onSlack, onAny, ofChat, ofChatOtherPlatform)
    resourceAssert.use { case (onIrc, onTelegram, onSlack, onAny, ofChat, ofChatOtherPlatform) =>
      IO {
        assertEquals(onIrc.map(_.id).toSet.intersect(ours), Set(irc.id))
        assertEquals(onTelegram.map(_.id).toSet.intersect(ours), Set(telegram.id))
        assertEquals(onSlack.map(_.id).toSet.intersect(ours), Set.empty[String])
        assertEquals(onAny.map(_.id).toSet.intersect(ours), ours)
        assertEquals(ofChat.map(_.id), List(irc.id))
        assertEquals(ofChatOtherPlatform, List.empty)
      }
    }
  }
}
