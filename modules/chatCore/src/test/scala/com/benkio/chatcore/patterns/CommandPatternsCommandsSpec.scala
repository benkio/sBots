package com.benkio.chatcore.patterns

import cats.effect.IO
import cats.syntax.all.*
import com.benkio.chatcore.mocks.BackgroundJobManagerMock
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionId
import com.benkio.chatcore.model.SubscriptionType
import com.benkio.chatcore.model.Timeout
import com.benkio.chatcore.repository.db.DBMessageReplies
import com.benkio.chatcore.repository.db.DBShowData
import com.benkio.chatcore.repository.db.DBTimeoutData
import com.benkio.chatcore.Logger.given
import com.benkio.chatcore.TestIds
import cron4s.Cron
import munit.CatsEffectSuite

import java.time.Instant
import java.util.UUID

class CommandPatternsCommandsSpec extends CatsEffectSuite {

  private val sBotInfo = SBotInfo(
    botName = SBotInfo.SBotName("SampleWebhookBot"),
    botId = SBotInfo.SBotId("sbot")
  )
  private val chatId = TestIds.chatId(123L)
  private val msg    = Message(
    messageId = None,
    date = 0L,
    chatId = chatId,
    chatType = "private"
  )

  test("InstructionsCommand should render italian instructions when input is 'it'") {
    val command = CommandPatterns.RandomDataCommand.randomDataReplyBundleCommand(sBotInfo)
    val result  = CommandPatterns.InstructionsCommand.instructionCommandLogic[IO](
      msg = msg.copy(text = Some("/instructions it")),
      sBotInfo = sBotInfo,
      ignoreMessagePrefix = Some("!"),
      commands = List(command),
      ttl = None
    )

    result.map {
      case Text(value, _) =>
        assert(value.contains("---- Instruzioni Per SampleWebhookBot ----"))
        assert(value.contains("Se si vuole disabilitare il bot"))
      case other =>
        fail(s"Expected text instructions, got: ${other.show}")
    }
  }

  test("SetTimeoutCommand should remove timeout when command input is empty") {
    val timeout = Timeout(chatId, sBotInfo.botId, "00:00:10").toOption.get
    val dbLayer = DBLayerMock.mock(
      botId = sBotInfo.botId,
      timeouts = List(DBTimeoutData(timeout))
    )

    val result = for {
      reply <- CommandPatterns.SetTimeoutCommand.setTimeoutLogic[IO](
        msg = msg.copy(text = Some("/settimeout ")),
        dbTimeout = dbLayer.dbTimeout,
        sBotInfo = sBotInfo,
        ttl = None
      )
      timeoutAfter <- dbLayer.dbTimeout.getOrDefault(chatId = chatId, botId = sBotInfo.botId)
    } yield (reply, timeoutAfter)

    result.map { case (reply, timeoutAfter) =>
      assertEquals(reply, Text("Timeout removed"))
      assertEquals(timeoutAfter.timeout_value, "0")
    }
  }

  test("GetTimeoutCommand should return currently configured timeout") {
    val timeout = Timeout(chatId, sBotInfo.botId, "00:00:10").toOption.get
    val dbLayer = DBLayerMock.mock(
      botId = sBotInfo.botId,
      timeouts = List(DBTimeoutData(timeout))
    )

    val result = CommandPatterns.GetTimeoutCommand.getTimeoutLogic[IO](
      msg = msg.copy(text = Some("/gettimeout")),
      dbTimeout = dbLayer.dbTimeout,
      sBotInfo = sBotInfo,
      ttl = None
    )

    assertIO(result, Text("The Timeout is 00:00:10.000"))
  }

  test("Subscribe command should schedule and then unsubscribe command should cancel by id") {
    val manager              = BackgroundJobManagerMock.mock()
    val existingSubscription = Subscription(
      id = SubscriptionId(UUID.fromString("B674CCE0-9684-4D31-8CC7-9E2A41EA0878")),
      chatId = chatId,
      botId = sBotInfo.botId,
      cron = Cron.unsafeParse("0 * * ? * *"),
      subscribedAt = Instant.now()
    )

    val result = for {
      subscribeReply <- CommandPatterns.SubscribeUnsubscribeCommand.subscribeCommandLogic[IO](
        backgroundJobManager = manager,
        dbMessageReplies = messageReplies,
        m = msg.copy(text = Some("/subscribe 0 * * ? * *")),
        sBotInfo = sBotInfo,
        ttl = None
      )
      scheduledAfterSubscribe = manager.getScheduledSubscriptions()
      _                <- manager.scheduleSubscription(existingSubscription)
      unsubscribeReply <- CommandPatterns.SubscribeUnsubscribeCommand.unsubcribeCommandLogic[IO](
        backgroundJobManager = manager,
        m = msg.copy(text = Some(s"/unsubscribe ${existingSubscription.id.value}")),
        sBotInfo = sBotInfo,
        ttl = None
      )
      scheduledAfterUnsubscribe = manager.getScheduledSubscriptions()
    } yield (subscribeReply, scheduledAfterSubscribe, unsubscribeReply, scheduledAfterUnsubscribe)

    result.map { case (subscribeReply, scheduledAfterSubscribe, unsubscribeReply, scheduledAfterUnsubscribe) =>
      assert(subscribeReply.show.startsWith("Subscription successfully scheduled. Next occurrence of subscription is "))
      assertEquals(scheduledAfterSubscribe.size, 1)
      assertEquals(unsubscribeReply, Text("Subscription successfully cancelled"))
      assertEquals(scheduledAfterUnsubscribe.count(_.subscriptionId == existingSubscription.id), 0)
    }
  }

  // The only message reply of the bot: the text of a trigger subscription must select it
  private val messageReplies: DBMessageReplies =
    DBMessageReplies(List(ReplyBundleMessage.textToText("buongiorno")("ciao")))

  private def subscribe(
      manager: com.benkio.chatcore.BackgroundJobManager[IO],
      text: String,
      chat: ChatId = chatId,
      replies: DBMessageReplies = messageReplies
  ) =
    CommandPatterns.SubscribeUnsubscribeCommand.subscribeCommandLogic[IO](
      backgroundJobManager = manager,
      dbMessageReplies = replies,
      m = msg.copy(chatId = chat, text = Some(text)),
      sBotInfo = sBotInfo,
      ttl = None
    )

  private def unsubscribe(manager: com.benkio.chatcore.BackgroundJobManager[IO], text: String, chat: ChatId = chatId) =
    CommandPatterns.SubscribeUnsubscribeCommand.unsubcribeCommandLogic[IO](
      backgroundJobManager = manager,
      m = msg.copy(chatId = chat, text = Some(text)),
      sBotInfo = sBotInfo,
      ttl = None
    )

  private def subscriptionOf(
      subscriptionType: SubscriptionType,
      chat: ChatId = chatId,
      target: Option[String] = None
  ): Subscription =
    Subscription(
      id = SubscriptionId(UUID.randomUUID()),
      chatId = chat,
      botId = sBotInfo.botId,
      subscriptionType = subscriptionType,
      target = target,
      cron = Cron.unsafeParse("0 * * ? * *"),
      subscribedAt = Instant.now()
    )

  test("Subscribe command should accept the show type with and without target") {
    val manager = BackgroundJobManagerMock.mock()
    for {
      plain      <- subscribe(manager = manager, text = "/subscribe 0 * * ? * *")
      showOnly   <- subscribe(manager = manager, text = "/subscribe 0 * * ? * * show")
      withTarget <- subscribe(manager = manager, text = "/subscribe 0 * * ? * * show pino scotto")
    } yield {
      List(plain, showOnly, withTarget).foreach(reply =>
        assert(reply.show.startsWith("Subscription successfully scheduled."), reply.show)
      )
      assertEquals(manager.getScheduledSubscriptions().map(_.subscriptionType), List.fill(3)(SubscriptionType.Show))
    }
  }

  test("Subscribe command should schedule a trigger subscription with its text as target") {
    val manager = BackgroundJobManagerMock.mock()
    subscribe(manager = manager, text = "/subscribe 0 0 9 * * ? trigger buongiorno a tutti").map { reply =>
      assert(reply.show.startsWith("Subscription successfully scheduled."), reply.show)
      assertEquals(manager.getScheduledSubscriptions().map(_.subscriptionType), List(SubscriptionType.Trigger))
    }
  }

  test("Subscribe command should reject a trigger subscription whose text selects no reply and schedule nothing") {
    val manager = BackgroundJobManagerMock.mock()
    List("arrivederci", "buon giorno").traverse { text =>
      subscribe(manager = manager, text = s"/subscribe 0 0 9 * * ? trigger $text").map(reply =>
        assert(reply.show.contains(s"The text `$text` triggers no reply of the bot"), reply.show)
      )
    } *> IO(assertEquals(manager.getScheduledSubscriptions(), List.empty))
  }

  test("Subscribe command should reject a trigger subscription when the bot has no replies at all") {
    val manager = BackgroundJobManagerMock.mock()
    subscribe(manager = manager, text = "/subscribe 0 0 9 * * ? trigger buongiorno", replies = DBMessageReplies.empty)
      .map { reply =>
        assert(reply.show.contains("triggers no reply of the bot"), reply.show)
        assertEquals(manager.getScheduledSubscriptions(), List.empty)
      }
  }

  test("Subscribe command should not look at the replies of the bot for a show subscription") {
    val manager = BackgroundJobManagerMock.mock()
    subscribe(manager = manager, text = "/subscribe 0 0 9 * * ? show anything", replies = DBMessageReplies.empty)
      .map { reply =>
        assert(reply.show.startsWith("Subscription successfully scheduled."), reply.show)
        assertEquals(manager.getScheduledSubscriptions().map(_.subscriptionType), List(SubscriptionType.Show))
      }
  }

  test("Subscribe command should schedule a trigger subscription without text: a random reply of the bot") {
    val manager = BackgroundJobManagerMock.mock()
    subscribe(manager = manager, text = "/subscribe 0 0 9 * * ? trigger").map { reply =>
      assert(reply.show.startsWith("Subscription successfully scheduled."), reply.show)
      assertEquals(manager.getScheduledSubscriptions().map(_.subscriptionType), List(SubscriptionType.Trigger))
    }
  }

  test("Subscribe command should reject a trigger subscription without text when the bot has no replies") {
    val manager = BackgroundJobManagerMock.mock()
    subscribe(manager = manager, text = "/subscribe 0 0 9 * * ? trigger", replies = DBMessageReplies.empty).map {
      reply =>
        assert(reply.show.contains("The bot has no replies to trigger"), reply.show)
        assertEquals(manager.getScheduledSubscriptions(), List.empty)
    }
  }

  test("Subscribe command should reject a cron that fires more often than once a minute") {
    val manager = BackgroundJobManagerMock.mock()
    List("* * * ? * *", "*/30 * * ? * *", "0,59 * * ? * *").traverse { cron =>
      subscribe(manager = manager, text = s"/subscribe $cron").map(reply =>
        assert(reply.show.contains("The subscription interval is too short"), reply.show)
      )
    } *> IO(assertEquals(manager.getScheduledSubscriptions(), List.empty))
  }

  test("Subscribe command should reject unsupported types and schedule nothing") {
    val manager = BackgroundJobManagerMock.mock()
    List("calendar", "hello").traverse { token =>
      subscribe(manager = manager, text = s"/subscribe 0 * * ? * * $token something").map(reply =>
        assert(reply.show.contains(s"Unsupported subscription type '$token'"), reply.show)
      )
    } *> IO(assertEquals(manager.getScheduledSubscriptions(), List.empty))
  }

  test("Subscribe command should reject a too long target") {
    val manager = BackgroundJobManagerMock.mock()
    subscribe(manager = manager, text = s"/subscribe 0 * * ? * * show ${"a" * 201}").map { reply =>
      assert(reply.show.contains("The subscription target is too long"), reply.show)
      assertEquals(manager.getScheduledSubscriptions(), List.empty)
    }
  }

  test("Subscribe command should reject an invalid cron") {
    val manager = BackgroundJobManagerMock.mock()
    subscribe(manager = manager, text = "/subscribe not a cron at all really").map { reply =>
      assert(reply.show.startsWith("An error occurred processing the command: subscribe"), reply.show)
      assertEquals(manager.getScheduledSubscriptions(), List.empty)
    }
  }

  test("Unsubscribe command with a type should cancel only the subscriptions of that type for that chat") {
    val manager      = BackgroundJobManagerMock.mock()
    val otherChat    = TestIds.chatId(999L)
    val show         = subscriptionOf(subscriptionType = SubscriptionType.Show)
    val calendar     = subscriptionOf(subscriptionType = SubscriptionType.Calendar)
    val otherChatCal = subscriptionOf(subscriptionType = SubscriptionType.Calendar, chat = otherChat)
    for {
      _     <- List(show, calendar, otherChatCal).traverse_(manager.scheduleSubscription)
      reply <- unsubscribe(manager = manager, text = "/unsubscribe calendar")
    } yield {
      assertEquals(reply, Text("All calendar subscriptions for current chat successfully cancelled"))
      assertEquals(manager.getScheduledSubscriptions().map(_.subscriptionId).toSet, Set(show.id, otherChatCal.id))
    }
  }

  test("Unsubscribe command with a type should do nothing when there is none") {
    val manager = BackgroundJobManagerMock.mock()
    val show    = subscriptionOf(subscriptionType = SubscriptionType.Show)
    for {
      _     <- manager.scheduleSubscription(show)
      reply <- unsubscribe(manager = manager, text = "/unsubscribe trigger")
    } yield {
      assertEquals(reply, Text("All trigger subscriptions for current chat successfully cancelled"))
      assertEquals(manager.getScheduledSubscriptions().map(_.subscriptionId), List(show.id))
    }
  }

  test("Unsubscribe command without input should cancel everything of the chat") {
    val manager   = BackgroundJobManagerMock.mock()
    val otherChat = subscriptionOf(subscriptionType = SubscriptionType.Show, chat = TestIds.chatId(999L))
    for {
      _ <- List(
        subscriptionOf(subscriptionType = SubscriptionType.Show),
        subscriptionOf(subscriptionType = SubscriptionType.Calendar),
        otherChat
      )
        .traverse_(manager.scheduleSubscription)
      reply <- unsubscribe(manager = manager, text = "/unsubscribe")
    } yield {
      assertEquals(reply, Text("All Subscriptions for current chat successfully cancelled"))
      assertEquals(manager.getScheduledSubscriptions().map(_.subscriptionId), List(otherChat.id))
    }
  }

  test("Unsubscribe command should explain the accepted inputs when the input is invalid") {
    val manager = BackgroundJobManagerMock.mock()
    unsubscribe(manager = manager, text = "/unsubscribe foo").map { reply =>
      assert(reply.show.contains("Invalid input 'foo'"), reply.show)
      assert(reply.show.contains("show, trigger, calendar"), reply.show)
    }
  }
  private def calendarShows: List[DBShowData] =
    List(
      DBShowData(
        show_id = "abc",
        bot_id = sBotInfo.botId.value,
        show_title = "A show 17 maggio 2019",
        show_upload_date = "2019-05-17T00:00:00.000Z",
        show_duration = 3600,
        show_description = None,
        show_is_live = false,
        show_origin_automatic_caption = None,
        show_origin_automatic_caption_srt = ""
      )
    )

  private def dbLayerWith(shows: List[DBShowData]) = DBLayerMock.mock(botId = sBotInfo.botId, shows = shows)

  private def loadCalendar(
      manager: com.benkio.chatcore.BackgroundJobManager[IO],
      shows: List[DBShowData] = calendarShows,
      chat: ChatId = chatId
  ) =
    CommandPatterns.CalendarCommands.loadCalendarCommandLogic[IO](
      backgroundJobManager = manager,
      dbShow = dbLayerWith(shows).dbShow,
      m = msg.copy(chatId = chat, text = Some("/loadcalendar")),
      sBotInfo = sBotInfo,
      ttl = None
    )

  test(
    "LoadCalendar command should schedule one calendar subscription and reply with the id and the unsubscribe hint"
  ) {
    val manager = BackgroundJobManagerMock.mock()
    loadCalendar(manager = manager).map { reply =>
      val scheduled = manager.getScheduledSubscriptions()
      assertEquals(scheduled.map(_.subscriptionType), List(SubscriptionType.Calendar))
      assert(reply.show.contains(scheduled.head.subscriptionId.value.toString), reply.show)
      assert(reply.show.contains(s"/unsubscribe ${scheduled.head.subscriptionId.value}"), reply.show)
    }
  }

  test("LoadCalendar command should reject a second calendar for the same chat and keep the first") {
    val manager = BackgroundJobManagerMock.mock()
    for {
      _      <- loadCalendar(manager = manager)
      first  <- IO(manager.getScheduledSubscriptions())
      second <- loadCalendar(manager = manager)
      after  <- IO(manager.getScheduledSubscriptions())
    } yield {
      assert(second.show.contains("calendar subscription already active"), second.show)
      assertEquals(after, first)
    }
  }

  test("LoadCalendar command should allow another chat and, after /unsubscribe <uuid>, the same chat again") {
    val manager = BackgroundJobManagerMock.mock()
    for {
      _        <- loadCalendar(manager = manager)
      other    <- loadCalendar(manager = manager, chat = TestIds.chatId(456L))
      id       <- IO(manager.getScheduledSubscriptions().find(_.chatId == chatId).get.subscriptionId)
      _        <- unsubscribe(manager = manager, text = s"/unsubscribe ${id.value}")
      reloaded <- loadCalendar(manager = manager)
    } yield {
      assert(other.show.startsWith("Calendar successfully scheduled"), other.show)
      assert(reloaded.show.startsWith("Calendar successfully scheduled"), reloaded.show)
      assertEquals(manager.getScheduledSubscriptions().size, 2)
    }
  }

  test("LoadCalendar command should schedule nothing for a bot without shows") {
    val manager = BackgroundJobManagerMock.mock()
    loadCalendar(manager = manager, shows = List.empty).map { reply =>
      assertEquals(reply, Text("no calendar"))
      assertEquals(manager.getScheduledSubscriptions(), List.empty)
    }
  }

  test("ShowCalendar command should link the committed calendar file or say there is no calendar") {
    for {
      withShows <- CommandPatterns.CalendarCommands.showCalendarCommandLogic[IO](
        dbShow = dbLayerWith(calendarShows).dbShow,
        sBotInfo = sBotInfo,
        ttl = None
      )
      empty <- CommandPatterns.CalendarCommands.showCalendarCommandLogic[IO](
        dbShow = dbLayerWith(List.empty).dbShow,
        sBotInfo = sBotInfo,
        ttl = None
      )
    } yield {
      assertEquals(
        withShows,
        Text("https://github.com/benkio/sBots/blob/main/modules/bots/SampleWebhookBot/sbot_calendar.ics")
      )
      assertEquals(empty, Text("no calendar"))
    }
  }
}
