package com.benkio.chattelegramadapter

import cats.effect.IO
import com.benkio.chatcore.messagefiltering.FilteringOlder
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionId
import com.benkio.chatcore.model.SubscriptionType
import com.benkio.chatcore.Logger.given
import com.benkio.chattelegramadapter.mocks.PayloadRecordingApi
import com.benkio.chattelegramadapter.mocks.SampleWebhookBot
import cron4s.Cron
import io.circe.Json
import munit.CatsEffectSuite
import telegramium.bots.high.Api

import java.time.Instant
import java.util.UUID

/** A trigger subscription fires from the background job manager, with the replies served by the `DBLayer`. */
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

  // Fires the subscription of a bot whose message replies are the given ones, returning the api calls it made
  private def fire(
      messageReplies: List[ReplyBundleMessage],
      subscription: Subscription
  ): IO[List[(String, Json)]] =
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      bot   <- SampleWebhookBot(Some(messageReplies))(using summon, api: Api[IO])
      fired <- bot.backgroundJobManager.runSubscription(subscription)
      _     <- fired._1.compile.drain
      calls <- recorded
    } yield calls

  test("a fresh-dated message passes the FilteringOlder filter, a message with date = 0 does not") {
    val bundle = replyingBundle(trigger = "hello", reply = "hi", replyToMessage = false)
    val select = (m: Message) =>
      ReplyBundleMessage.selectReplyBundle(m, List(bundle), ignoreMessagePrefix = None, disableForward = false)
    assertEquals(select(message("hello")), Some(bundle))
    assertEquals(select(message("hello", date = 0L)), None)
    assert(FilteringOlder.filter(message("hello")))
  }

  test("a matching text gets the reply a live message would get, not attached to the synthetic message") {
    fire(
      List(replyingBundle(trigger = "hello", reply = "hi there", replyToMessage = true)),
      triggerSubscription(Some("say hello"))
    ).map { calls =>
      assertEquals(calls.map(_._1), List("sendChatAction", "sendMessage"))
      val payload = calls.last._2
      assertEquals(payload.hcursor.get[String]("text"), Right("hi there"))
      assert(payload.hcursor.downField("reply_parameters").failed, payload.noSpaces)
    }
  }

  test("the text is matched like a live message: the longest matching trigger wins") {
    val bundles = List(
      replyingBundle(trigger = "hello", reply = "short", replyToMessage = false),
      replyingBundle(trigger = "hello world", reply = "long", replyToMessage = false)
    )
    fire(bundles, triggerSubscription(Some("hello world"))).map(calls =>
      assertEquals(calls.last._2.hcursor.get[String]("text"), Right("long"))
    )
  }

  test("a text matching nothing sends nothing") {
    fire(
      List(replyingBundle(trigger = "hello", reply = "hi", replyToMessage = false)),
      triggerSubscription(Some("something else"))
    )
      .map(calls => assertEquals(calls, List.empty))
  }

  test("the ignore prefix of the bot does not apply: the text just triggers what it matches") {
    fire(
      List(replyingBundle(trigger = "hello", reply = "hi", replyToMessage = false)),
      triggerSubscription(Some("!hello"))
    ).map(calls => assertEquals(calls.map(_._1), List("sendChatAction", "sendMessage")))
  }

  test("a trigger subscription without text sends nothing") {
    fire(List(replyingBundle(trigger = "hello", reply = "hi", replyToMessage = false)), triggerSubscription(None))
      .map(calls => assertEquals(calls, List.empty))
  }

  test("a failing reply does not make the fire fail") {
    // no value to pick from: the computation of the reply raises an error
    val broken = ReplyBundleMessage.textToText("hello")("x").copy(reply = TextReply(text = Set.empty))
    fire(List(broken), triggerSubscription(Some("hello"))).map(calls => assertEquals(calls, List.empty))
  }

  test("the other subscription types are not affected: a show subscription still replies to the message") {
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      bot   <- SampleWebhookBot()(using summon, api: Api[IO])
      fired <- bot.backgroundJobManager.runSubscription(
        triggerSubscription(Some("test")).copy(subscriptionType = SubscriptionType.Calendar, target = None)
      )
      _     <- fired._1.compile.drain
      calls <- recorded
    } yield assertEquals(calls, List.empty) // a calendar with no show of the day sends nothing
  }
}
