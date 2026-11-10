package com.benkio.chattelegramadapter

import cats.effect.kernel.Ref
import cats.effect.IO
import com.benkio.chatcore.messagefiltering.FilteringOlder
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionId
import com.benkio.chatcore.model.SubscriptionType
import com.benkio.chatcore.Logger.given
import com.benkio.chattelegramadapter.mocks.ApiMock.given
import com.benkio.chattelegramadapter.mocks.PayloadRecordingApi
import com.benkio.chattelegramadapter.mocks.SampleWebhookBot
import cron4s.Cron
import munit.CatsEffectSuite

import java.time.Instant
import java.util.UUID

class ScheduledTriggerSpec extends CatsEffectSuite {

  private val chatId = ChatId(0L)

  private def message(text: String, date: Long = Instant.now.getEpochSecond): Message =
    Message(messageId = 0, date = date, chatId = chatId, chatType = "private", text = Some(text))

  private def replyingBundle(trigger: String, reply: String, replyToMessage: Boolean): ReplyBundleMessage =
    ReplyBundleMessage.textToText(trigger)(reply).copy(reply = TextReply.fromList(reply)(replyToMessage))

  private def triggerSubscription(target: Option[String]): Subscription = Subscription(
    id = SubscriptionId(UUID.randomUUID()),
    chatId = chatId,
    botId = SampleWebhookBot.sBotInfo.botId,
    subscriptionType = SubscriptionType.Trigger,
    target = target,
    cron = Cron.unsafeParse("0 0 9 * * ?"),
    subscribedAt = Instant.now()
  )

  // The bot with the given message replies, driven with a recording api
  private def withBot[A](
      messageReplies: List[ReplyBundleMessage],
      customize: SampleWebhookBot => SampleWebhookBot = identity
  )(test: (SampleWebhookBot, PayloadRecordingApi, IO[List[(String, io.circe.Json)]]) => IO[A]): IO[A] =
    for {
      base            <- SampleWebhookBot()
      bot             = customize(new SampleWebhookBot(base.sBotSetup, messageReplies, List.empty))
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      result <- test(bot, api, recorded)
    } yield result

  test("a fresh-dated message passes the FilteringOlder filter, a message with date = 0 does not") {
    val bundle = replyingBundle(trigger = "hello", reply = "hi", replyToMessage = false)
    val select = (m: Message) =>
      ReplyBundleMessage.selectReplyBundle(m, List(bundle), ignoreMessagePrefix = None, disableForward = false)
    assertEquals(select(message("hello")), Some(bundle))
    assertEquals(select(message("hello", date = 0L)), None)
    assert(FilteringOlder.filter(message("hello")))
  }

  test("a matching text gets the reply a live message would get, not attached to the synthetic message") {
    withBot(List(replyingBundle(trigger = "hello", reply = "hi there", replyToMessage = true))) { (bot, api, recorded) =>
      for {
        _     <- bot.scheduledTriggerLogic(message("hello"))(using api)
        calls <- recorded
      } yield {
        assertEquals(calls.map(_._1), List("sendChatAction", "sendMessage"))
        val payload = calls.last._2
        assertEquals(payload.hcursor.get[String]("text"), Right("hi there"))
        assert(payload.hcursor.downField("reply_parameters").failed, payload.noSpaces)
      }
    }
  }

  test("a text matching nothing sends nothing") {
    withBot(List(replyingBundle(trigger = "hello", reply = "hi", replyToMessage = false))) { (bot, api, recorded) =>
      bot.scheduledTriggerLogic(message("something else"))(using api) *> recorded.map(calls =>
        assertEquals(calls, List.empty)
      )
    }
  }

  test("a text matching an effectful reply runs the effectful key") {
    val effectful = ReplyBundleMessage(
      trigger = replyingBundle(trigger = "subs", reply = "unused", replyToMessage = false).trigger,
      reply = EffectfulReply(key = EffectfulKey.Subscriptions(SampleWebhookBot.sBotInfo)),
      matcher = replyingBundle(trigger = "subs", reply = "unused", replyToMessage = false).matcher
    )
    withBot(List(effectful)) { (bot, api, recorded) =>
      for {
        _     <- bot.scheduledTriggerLogic(message("subs"))(using api)
        calls <- recorded
      } yield {
        assertEquals(calls.map(_._1), List("sendChatAction", "sendRichMessage"))
        assert(calls.last._2.hcursor.downField("reply_parameters").failed, calls.last._2.noSpaces)
      }
    }
  }

  test("the timeout filter and the post computation are skipped") {
    Ref.of[IO, Boolean](false).flatMap { postComputationRan =>
      withBot(
        List(replyingBundle(trigger = "hello", reply = "hi", replyToMessage = false)),
        customize = bot =>
          new SampleWebhookBot(bot.sBotSetup, bot.messageRepliesData, bot.commandRepliesData) {
            override def filteringMatchesMessages: (ReplyBundleMessage, Message) => IO[Boolean] =
              (_, _) => IO.pure(false) // a live message would be filtered out
            override def postComputation: Message => IO[Unit] = _ => postComputationRan.set(true)
          }
      ) { (bot, api, recorded) =>
        for {
          _     <- bot.scheduledTriggerLogic(message("hello"))(using api)
          calls <- recorded
          ran   <- postComputationRan.get
        } yield {
          assertEquals(calls.map(_._1), List("sendChatAction", "sendMessage"))
          assert(!ran)
        }
      }
    }
  }

  test("a registered bot replies to a fired trigger subscription as to a live message") {
    withBot(List(replyingBundle(trigger = "hello", reply = "hi", replyToMessage = false))) { (bot, api, recorded) =>
      for {
        _     <- bot.registerTriggerHandler()(using api)
        fired <- bot.backgroundJobManager.runSubscription(triggerSubscription(Some("say hello please")))
        _     <- fired._1.compile.drain
        calls <- recorded
      } yield assertEquals(calls.map(_._1), List("sendChatAction", "sendMessage"))
    }
  }

  test("a trigger subscription fires the handler with the target as text and a current date") {
    for {
      base     <- SampleWebhookBot()
      received <- Ref.of[IO, List[Message]](List.empty)
      before   <- IO(Instant.now.getEpochSecond)
      _        <- base.backgroundJobManager.setTriggerHandler(m => received.update(_ :+ m))
      fired    <- base.backgroundJobManager.runSubscription(triggerSubscription(Some("say hello")))
      _        <- fired._1.compile.drain
      after    <- IO(Instant.now.getEpochSecond)
      messages <- received.get
    } yield {
      assertEquals(messages.map(_.text), List(Some("say hello")))
      assertEquals(messages.map(_.chatId), List(chatId))
      assert(messages.head.date >= before && messages.head.date <= after, messages.toString)
    }
  }

  test("a trigger firing without any registered handler sends nothing and does not fail") {
    for {
      base            <- SampleWebhookBot()
      apiAndRecording <- PayloadRecordingApi()
      fired           <- base.backgroundJobManager.runSubscription(triggerSubscription(Some("hello")))
      _               <- fired._1.compile.drain
      calls           <- apiAndRecording._2
    } yield assertEquals(calls, List.empty)
  }

  test("a trigger subscription without text fires nothing") {
    for {
      base     <- SampleWebhookBot()
      received <- Ref.of[IO, List[Message]](List.empty)
      _        <- base.backgroundJobManager.setTriggerHandler(m => received.update(_ :+ m))
      fired    <- base.backgroundJobManager.runSubscription(triggerSubscription(None))
      _        <- fired._1.compile.drain
      messages <- received.get
    } yield assertEquals(messages, List.empty)
  }

  test("a failing handler does not stop the subscription") {
    for {
      base  <- SampleWebhookBot()
      _     <- base.backgroundJobManager.setTriggerHandler(_ => IO.raiseError(new RuntimeException("boom")))
      fired <- base.backgroundJobManager.runSubscription(triggerSubscription(Some("hello")))
      _     <- fired._1.compile.drain
    } yield ()
  }
}
