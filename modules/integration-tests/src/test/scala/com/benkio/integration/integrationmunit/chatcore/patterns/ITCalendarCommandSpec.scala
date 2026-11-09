package com.benkio.integration.integrationmunit.chatcore.patterns

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionType
import com.benkio.chatcore.patterns.CommandPatterns.CalendarCommands
import com.benkio.chatcore.patterns.CommandPatterns.SubscribeUnsubscribeCommand
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.db.DBShowData
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chattelegramadapter.mocks.ApiMock.given
import com.benkio.chattelegramadapter.SBot
import com.benkio.chattelegramadapter.TelegramBackgroundJobManager
import com.benkio.integration.DBFixture
import com.benkio.integration.DBFixtureResources
import com.benkio.integration.Logger.given
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import munit.CatsEffectSuite

class ITCalendarCommandSpec extends CatsEffectSuite with DBFixture {

  val sBotConfig              = SBot.buildSBotConfig(RichardPHJBensonBot.sBotInfo)
  val sBotInfo                = sBotConfig.sBotInfo
  val otherBotInfo: SBotInfo  = sBotInfo.copy(botId = SBotInfo.SBotId("otherbot"))
  val chatId                  = ChatId(1L)
  val msg: Message            = Message(messageId = 0, date = 0, chatId = chatId, chatType = "private")
  val alreadyActive           = "calendar subscription already active"

  private def show(botInfo: SBotInfo): DBShowData = DBShowData(
    show_id = s"show-${botInfo.botId.value}",
    bot_id = botInfo.botId.value,
    show_title = "A show 17 maggio 2019",
    show_upload_date = "2019-05-17T00:00:00.000Z",
    show_duration = 3600,
    show_description = None,
    show_is_live = false,
    show_origin_automatic_caption = None,
    show_origin_automatic_caption_srt = ""
  )

  private def manager(
      fixture: DBFixtureResources,
      dbLayer: DBLayer[IO],
      botInfo: SBotInfo = sBotInfo
  ): Resource[IO, BackgroundJobManager[IO]] =
    fixture.repositoryResource.evalMap(repository =>
      TelegramBackgroundJobManager(
        repository = repository,
        dbLayer = dbLayer,
        sBotInfo = botInfo,
        ttl = sBotConfig.messageTimeToLive
      )
    )

  private def loadCalendar(
      jobManager: BackgroundJobManager[IO],
      dbLayer: DBLayer[IO],
      botInfo: SBotInfo = sBotInfo,
      chat: ChatId = chatId
  ): IO[ReplyValue] =
    CalendarCommands.loadCalendarCommandLogic[IO](
      backgroundJobManager = jobManager,
      dbShow = dbLayer.dbShow,
      m = msg.copy(chatId = chat, text = Some("/loadcalendar")),
      sBotInfo = botInfo,
      ttl = None
    )

  private def calendars(dbLayer: DBLayer[IO], botInfo: SBotInfo = sBotInfo) =
    dbLayer.dbSubscription
      .getSubscriptions(botInfo.botId)
      .map(_.filter(_.subscription_type == SubscriptionType.Calendar.asString))

  private val setup = (fixture: DBFixtureResources) =>
    for {
      dbLayer <- fixture.resourceDBLayer
      // the DB is shared with the other specs: leave it as it was found
      _ <- Resource.make(List(sBotInfo, otherBotInfo).traverse_(b => dbLayer.dbShow.insertShow(show(b))))(_ =>
        List(sBotInfo, otherBotInfo).traverse_(b => dbLayer.dbShow.deleteShow(show(b))) *>
          List(chatId, ChatId(2L)).traverse_(c => dbLayer.dbSubscription.deleteSubscriptions(c.value))
      )
      jobManager <- manager(fixture = fixture, dbLayer = dbLayer)
    } yield (dbLayer, jobManager)

  databaseFixture.test("LoadCalendar should store and schedule one calendar subscription at 09:00") { fixture =>
    setup(fixture).use { case (dbLayer, jobManager) =>
      for {
        reply  <- loadCalendar(jobManager = jobManager, dbLayer = dbLayer)
        stored <- calendars(dbLayer)
      } yield {
        assertEquals(stored.size, 1)
        assertEquals(stored.head.cron, "0 0 9 * * ?")
        assertEquals(jobManager.getScheduledSubscriptions().map(_.subscriptionType), List(SubscriptionType.Calendar))
        assert(reply.toString.contains(stored.head.id), reply.toString)
      }
    }
  }

  databaseFixture.test("a second LoadCalendar for the same chat and bot is rejected and the first stays") { fixture =>
    setup(fixture).use { case (dbLayer, jobManager) =>
      for {
        _      <- loadCalendar(jobManager = jobManager, dbLayer = dbLayer)
        first  <- calendars(dbLayer)
        reply  <- loadCalendar(jobManager = jobManager, dbLayer = dbLayer)
        stored <- calendars(dbLayer)
      } yield {
        assert(reply.toString.contains(alreadyActive), reply.toString)
        assertEquals(stored, first)
        assertEquals(jobManager.getScheduledSubscriptions().size, 1)
      }
    }
  }

  databaseFixture.test("LoadCalendar is allowed for another chat and for another bot") { fixture =>
    setup(fixture).use { case (dbLayer, jobManager) =>
      manager(fixture = fixture, dbLayer = dbLayer, botInfo = otherBotInfo).use { otherManager =>
        for {
          _           <- loadCalendar(jobManager = jobManager, dbLayer = dbLayer)
          otherChat   <- loadCalendar(jobManager = jobManager, dbLayer = dbLayer, chat = ChatId(2L))
          otherBot    <- loadCalendar(jobManager = otherManager, dbLayer = dbLayer, botInfo = otherBotInfo)
          stored      <- calendars(dbLayer)
          storedOther <- calendars(dbLayer, otherBotInfo)
        } yield {
          assert(!otherChat.toString.contains(alreadyActive), otherChat.toString)
          assert(!otherBot.toString.contains(alreadyActive), otherBot.toString)
          assertEquals(stored.size, 2)
          assertEquals(storedOther.size, 1)
        }
      }
    }
  }

  databaseFixture.test("after /unsubscribe <uuid> the calendar can be loaded again and is removed from memory and DB") {
    fixture =>
      setup(fixture).use { case (dbLayer, jobManager) =>
        for {
          _        <- loadCalendar(jobManager = jobManager, dbLayer = dbLayer)
          id       <- calendars(dbLayer).map(_.head.id)
          _        <- SubscribeUnsubscribeCommand.unsubcribeCommandLogic[IO](
            backgroundJobManager = jobManager,
            m = msg.copy(text = Some(s"/unsubscribe $id")),
            sBotInfo = sBotInfo,
            ttl = None
          )
          afterDb  <- calendars(dbLayer)
          afterMem = jobManager.getScheduledSubscriptions()
          reloaded <- loadCalendar(jobManager = jobManager, dbLayer = dbLayer)
          stored   <- calendars(dbLayer)
        } yield {
          assertEquals(afterDb, List.empty)
          assertEquals(afterMem, List.empty)
          assert(!reloaded.toString.contains(alreadyActive), reloaded.toString)
          assertEquals(stored.size, 1)
        }
      }
  }

  databaseFixture.test("a concurrent double LoadCalendar yields one row") { fixture =>
    setup(fixture).use { case (dbLayer, jobManager) =>
      for {
        replies <- List.fill(2)(loadCalendar(jobManager = jobManager, dbLayer = dbLayer)).parSequence
        stored  <- calendars(dbLayer)
      } yield {
        assertEquals(stored.size, 1)
        assertEquals(replies.count(_.toString.contains(alreadyActive)), 1)
      }
    }
  }

  databaseFixture.test("LoadCalendar for a bot without shows replies no calendar and schedules nothing") { fixture =>
    val noShowsBotInfo = sBotInfo.copy(botId = SBotInfo.SBotId("noshowsbot"))
    val noShows        = for {
      dbLayer    <- fixture.resourceDBLayer
      jobManager <- manager(fixture = fixture, dbLayer = dbLayer, botInfo = noShowsBotInfo)
    } yield (dbLayer, jobManager)
    noShows.use { case (dbLayer, jobManager) =>
      for {
        reply  <- loadCalendar(jobManager = jobManager, dbLayer = dbLayer, botInfo = noShowsBotInfo)
        stored <- calendars(dbLayer, noShowsBotInfo)
      } yield {
        assertEquals(reply.toString, "Text(no calendar,None)")
        assertEquals(stored, List.empty)
        assertEquals(jobManager.getScheduledSubscriptions(), List.empty)
      }
    }
  }

  databaseFixture.test("the calendar subscription is reloaded on restart and fires without errors") { fixture =>
    setup(fixture).use { case (dbLayer, jobManager) =>
      for {
        _       <- loadCalendar(jobManager = jobManager, dbLayer = dbLayer)
        stored  <- calendars(dbLayer).flatMap(_.traverse(s => IO.fromEither(Subscription(s))))
        fired   <- jobManager.runSubscription(stored.head).flatMap(_._1.compile.toList)
        restart <- manager(fixture = fixture, dbLayer = dbLayer).use(m => IO(m.getScheduledSubscriptions()))
      } yield {
        assertEquals(fired.size, 1)
        assertEquals(restart.map(_.subscriptionType), List(SubscriptionType.Calendar))
      }
    }
  }
}
