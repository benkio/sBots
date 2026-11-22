package com.benkio.chattelegramadapter.http.telegramreply.messagereply

import cats.effect.Async
import cats.effect.IO
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
}
