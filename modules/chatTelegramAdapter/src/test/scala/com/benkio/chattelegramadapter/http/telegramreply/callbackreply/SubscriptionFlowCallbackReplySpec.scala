package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import cats.effect.Async
import cats.effect.IO
import cats.effect.Ref
import com.benkio.chatcore.model.*
import com.benkio.chatcore.SubscriptionFlowPromptStore
import com.benkio.chattelegramadapter.mocks.PayloadRecordingApi
import com.benkio.chattelegramadapter.model.TelegramIds
import com.benkio.chattelegramadapter.model.TelegramIds.telegramId
import munit.CatsEffectSuite
import telegramium.bots.CallbackQuery
import telegramium.bots.Chat
import telegramium.bots.Message as TelegramMessage
import telegramium.bots.User as TelegramUser

import scala.concurrent.duration.*

class SubscriptionFlowCallbackReplySpec extends CatsEffectSuite {

  private val key = SubscriptionFlowKey(TelegramIds.chatId(10L), SBotInfo.SBotId("bot"), "1")

  private def query(userId: Long): CallbackQuery =
    CallbackQuery(
      id = "query-1",
      from = TelegramUser(id = userId, isBot = false, firstName = "user"),
      chatInstance = "instance",
      message = Some(TelegramMessage(messageId = 42, date = 0, chat = Chat(key.chatId.telegramId, "private"))),
      data = Some("flow-back")
    )

  test("a press by another user is rejected privately and emits no engine action") {
    for {
      store            <- IO.pure(SubscriptionFlowPromptStore.create[IO](1.minute, 10))
      _                <- store.put(key, "42", "1")
      actions          <- Ref.of[IO, Int](0)
      (api, recording) <- PayloadRecordingApi()
      _                <- SubscriptionFlowCallbackReply.reply[IO](
        query(2),
        SubscriptionFlowAction.back,
        store,
        (_, _, _) => actions.update(_ + 1)
      )(using Async[IO], api)
      calls <- recording
      count <- actions.get
    } yield {
      assertEquals(count, 0)
      assertEquals(calls.map(_._1), List("answerCallbackQuery"))
      assert(calls.head._2.noSpaces.contains("Only who started this subscription setup"), calls.head._2.noSpaces)
    }
  }

  test("the owner's press is acknowledged and submitted with the requester as author") {
    for {
      store            <- IO.pure(SubscriptionFlowPromptStore.create[IO](1.minute, 10))
      _                <- store.put(key, "42", "1")
      actions          <- Ref.of[IO, List[(Message, SubscriptionFlowAction, String)]](List.empty)
      (api, recording) <- PayloadRecordingApi()
      _                <- SubscriptionFlowCallbackReply.reply[IO](
        query(1),
        SubscriptionFlowAction.back,
        store,
        (msg, action, requester) => actions.update(_ :+ ((msg, action, requester)))
      )(using Async[IO], api)
      calls       <- recording
      submissions <- actions.get
    } yield {
      assertEquals(calls.map(_._1), List("answerCallbackQuery"))
      assertEquals(submissions.map(_._3), List("1"))
      assertEquals(submissions.head._2, SubscriptionFlowAction.back)
      assertEquals(submissions.head._1.authorId, Some("1"))
    }
  }

  test("a same-id prompt in another chat does not answer this chat's press") {
    for {
      store <- IO.pure(SubscriptionFlowPromptStore.create[IO](1.minute, 10))
      other = SubscriptionFlowKey(TelegramIds.chatId(11L), SBotInfo.SBotId("bot"), "1")
      _                <- store.put(other, "42", "1")
      actions          <- Ref.of[IO, Int](0)
      (api, recording) <- PayloadRecordingApi()
      _                <- SubscriptionFlowCallbackReply.reply[IO](
        query(1),
        SubscriptionFlowAction.back,
        store,
        (_, _, _) => actions.update(_ + 1)
      )(using Async[IO], api)
      calls <- recording
      count <- actions.get
    } yield {
      assertEquals(count, 0)
      assertEquals(calls.map(_._1), List("answerCallbackQuery"))
      assert(calls.head._2.noSpaces.contains("This prompt is not available anymore"), calls.head._2.noSpaces)
    }
  }

  test("a press on a prompt that is not in the store is rejected") {
    for {
      store            <- IO.pure(SubscriptionFlowPromptStore.create[IO](1.minute, 10))
      actions          <- Ref.of[IO, Int](0)
      (api, recording) <- PayloadRecordingApi()
      _                <- SubscriptionFlowCallbackReply.reply[IO](
        query(1),
        SubscriptionFlowAction.back,
        store,
        (_, _, _) => actions.update(_ + 1)
      )(using Async[IO], api)
      calls <- recording
      count <- actions.get
    } yield {
      assertEquals(count, 0)
      assertEquals(calls.map(_._1), List("answerCallbackQuery"))
      assert(calls.head._2.noSpaces.contains("This prompt is not available anymore"), calls.head._2.noSpaces)
    }
  }
}
