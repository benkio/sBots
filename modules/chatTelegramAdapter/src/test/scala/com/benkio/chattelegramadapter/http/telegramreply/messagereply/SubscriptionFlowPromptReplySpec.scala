package com.benkio.chattelegramadapter.http.telegramreply.messagereply

import cats.effect.Async
import cats.effect.IO
import cats.effect.Ref
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.SubscriptionDraftStep
import com.benkio.chatcore.model.SubscriptionFlowAction
import com.benkio.chatcore.model.SubscriptionFlowButton
import com.benkio.chatcore.model.SubscriptionFlowKey
import com.benkio.chatcore.model.SubscriptionFlowPrompt
import com.benkio.chatcore.model.SubscriptionType
import com.benkio.chatcore.SubscriptionFlowPromptStore
import com.benkio.chattelegramadapter.mocks.PayloadRecordingApi
import com.benkio.chattelegramadapter.model.TelegramIds
import munit.CatsEffectSuite
import telegramium.bots.client.Method
import telegramium.bots.high.Api
import telegramium.bots.high.FailedRequest

import scala.concurrent.duration.*

class SubscriptionFlowPromptReplySpec extends CatsEffectSuite {

  private val key = SubscriptionFlowKey(TelegramIds.chatId(10L), SBotId("bot"), "user-1")

  private val msg = Message(
    messageId = Some(TelegramIds.messageId(5)),
    date = 0L,
    chatId = key.chatId,
    chatType = "private"
  )

  private val firstPrompt = SubscriptionFlowPrompt(
    key = key,
    step = SubscriptionDraftStep.Type,
    text = "Choose the subscription type",
    buttons = List(List(SubscriptionFlowButton("show", SubscriptionFlowAction.chooseType(SubscriptionType.Show))))
  )

  /** Fails the first edit with the given Telegram description, then delegates normally. */
  private def failingEditApi(delegate: Api[IO], description: String, failed: Ref[IO, Boolean]): Api[IO] =
    new Api[IO] {
      def execute[Res](method: Method[Res]): IO[Res] =
        if method.payload.name != "editMessageText" then delegate.execute(method)
        else
          delegate.execute(method).flatMap { response =>
            failed.get.flatMap { happened =>
              if happened then IO.pure(response)
              else failed.set(true) *> IO.raiseError(FailedRequest(method, Some(400), Some(description)))
            }
          }
    }

  test("a missing-message edit failure replaces the handle and sends a new prompt") {
    for {
      store            <- IO.pure(SubscriptionFlowPromptStore.create[IO](1.minute, 10))
      (api, recording) <- PayloadRecordingApi()
      failed           <- Ref.of[IO, Boolean](false)
      broken = failingEditApi(delegate = api, description = "Bad Request: message to edit not found", failed = failed)
      _      <- SubscriptionFlowPromptReply.send[IO](firstPrompt, msg, false, store)(using Async[IO], api)
      before <- store.byKey(key)
      later = firstPrompt.copy(text = "later")
      _      <- SubscriptionFlowPromptReply.send[IO](later, msg, false, store)(using Async[IO], broken)
      calls  <- recording
      handle <- store.byKey(key)
    } yield {
      assertEquals(before.map(_.messageId), Some("0"))
      assertEquals(calls.map(_._1), List("sendMessage", "editMessageText", "sendMessage"))
      assertEquals(handle.map(_.messageId), Some("0"))
    }
  }

  test("an unrelated edit failure is propagated and keeps the handle") {
    for {
      store            <- IO.pure(SubscriptionFlowPromptStore.create[IO](1.minute, 10))
      (api, recording) <- PayloadRecordingApi()
      failed           <- Ref.of[IO, Boolean](false)
      broken = failingEditApi(delegate = api, description = "Bad Request: chat not found", failed = failed)
      _      <- SubscriptionFlowPromptReply.send[IO](firstPrompt, msg, false, store)(using Async[IO], api)
      result <- SubscriptionFlowPromptReply
        .send[IO](firstPrompt.copy(text = "later"), msg, false, store)(using Async[IO], broken)
        .attempt
      calls  <- recording
      handle <- store.byKey(key)
    } yield {
      assert(result.isLeft, result.toString)
      assertEquals(calls.map(_._1), List("sendMessage", "editMessageText"))
      assertEquals(handle.map(_.messageId), Some("0"))
    }
  }

  test("the first prompt is sent, later prompts of the same key edit the same message") {
    for {
      store           <- IO.pure(SubscriptionFlowPromptStore.create[IO](1.minute, 10))
      apiAndRecording <- PayloadRecordingApi()
      (api, recording) = apiAndRecording
      _      <- SubscriptionFlowPromptReply.send[IO](firstPrompt, msg, false, store)(using Async[IO], api)
      handle <- store.byKey(key)
      nextPrompt = firstPrompt.copy(
        step = SubscriptionDraftStep.Schedule,
        text = "Choose a schedule",
        buttons = List(List(SubscriptionFlowButton("back", SubscriptionFlowAction.back)))
      )
      _     <- SubscriptionFlowPromptReply.send[IO](nextPrompt, msg, false, store)(using Async[IO], api)
      calls <- recording
    } yield {
      assertEquals(handle.map(_.messageId), Some("0"))
      assertEquals(handle.map(_.owner), Some(key.requester))
      assertEquals(calls.map(_._1), List("sendMessage", "editMessageText"))
      val sent = calls.head._2.noSpaces
      val edit = calls.last._2.noSpaces
      assert(sent.contains("Choose the subscription type"), sent)
      assert(sent.contains("flow-type-show"), sent)
      assert(edit.contains("Choose a schedule"), edit)
      assert(edit.contains("flow-back"), edit)
      assert(edit.contains("\"message_id\":0"), edit)
    }
  }

  test("editing a prompt refreshes its handle so a long flow does not lose the prompt") {
    for {
      store            <- IO.pure(SubscriptionFlowPromptStore.create[IO](1.minute, 10))
      (api, recording) <- PayloadRecordingApi()
      _                <- SubscriptionFlowPromptReply.send[IO](firstPrompt, msg, false, store)(using Async[IO], api)
      before           <- store.byKey(key)
      _                <- IO.sleep(10.millis)
      _ <- SubscriptionFlowPromptReply.send[IO](firstPrompt.copy(text = "later"), msg, false, store)(using
        Async[IO],
        api
      )
      after <- store.byKey(key)
      calls <- recording
    } yield {
      assertEquals(calls.map(_._1), List("sendMessage", "editMessageText"))
      assertEquals(after.map(_.messageId), before.map(_.messageId))
      assert(after.exists(_.savedAt > before.get.savedAt))
    }
  }
}
