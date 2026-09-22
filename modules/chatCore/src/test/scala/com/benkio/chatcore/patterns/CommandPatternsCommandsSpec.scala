package com.benkio.chatcore.patterns

import cats.effect.IO
import cats.syntax.show.*
import com.benkio.chatcore.mocks.BackgroundJobManagerMock
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionId
import com.benkio.chatcore.model.Timeout
import com.benkio.chatcore.repository.db.DBTimeoutData
import com.benkio.chatcore.Logger.given
import cron4s.Cron
import munit.CatsEffectSuite

import java.time.Instant
import java.util.UUID

class CommandPatternsCommandsSpec extends CatsEffectSuite {

  private val sBotInfo = SBotInfo(
    botName = SBotInfo.SBotName("SampleWebhookBot"),
    botId = SBotInfo.SBotId("sbot")
  )
  private val chatId = ChatId(123L)
  private val msg    = Message(
    messageId = 0,
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
      case Text(value, _, _) =>
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
      timeoutAfter <- dbLayer.dbTimeout.getOrDefault(chatId = chatId.value, botId = sBotInfo.botId)
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
      cron = Cron.unsafeParse("* * * ? * *"),
      subscribedAt = Instant.now()
    )

    val result = for {
      subscribeReply <- CommandPatterns.SubscribeUnsubscribeCommand.subscribeCommandLogic[IO](
        backgroundJobManager = manager,
        m = msg.copy(text = Some("/subscribe * * * ? * *")),
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
}
