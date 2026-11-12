package com.benkio.chattelegramadapter.http

import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.Repository.RepositoryError
import com.benkio.chatcore.Notifier
import com.benkio.chattelegramadapter.mocks.PayloadRecordingApi
import com.benkio.chattelegramadapter.model.TelegramIds
import io.circe.Json
import munit.CatsEffectSuite

class TelegramNotifierSpec extends CatsEffectSuite {

  private val opsChatId: Long = -1001L
  private val message         =
    Message(Some(TelegramIds.messageId(1)), 0L, TelegramIds.chatId(5L), "private", text = Some("hello"))
  private val mediaFile = Mp3File("audio.mp3")
  private val botInfo   = SBotInfo(SBotInfo.SBotId("bot"), SBotInfo.SBotName("BotName"))

  private def sentMessages(calls: List[(String, Json)]): List[Json] =
    calls.collect { case ("sendMessage", payload) => payload }

  test("info sends the message to the chat of the operators") {
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      _     <- new TelegramNotifier[cats.effect.IO](opsChatId)(using summon, api).info(botInfo, "started")
      calls <- recorded
    } yield {
      val sent = sentMessages(calls)
      assertEquals(sent.size, 1)
      assertEquals(sent.head.hcursor.downField("chat_id").as[Long], Right(opsChatId))
      assertEquals(sent.head.hcursor.downField("text").as[String], Right(Notifier.formatInfo(botInfo, "started")))
    }
  }

  test("mediaError sends the error to the chat of the operators") {
    val error = new RuntimeException("boom")
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      _     <- new TelegramNotifier[cats.effect.IO](opsChatId)(using summon, api).mediaError(message, mediaFile, error)
      calls <- recorded
    } yield {
      val sent = sentMessages(calls)
      assertEquals(sent.size, 1)
      assertEquals(sent.head.hcursor.downField("chat_id").as[Long], Right(opsChatId))
      assertEquals(
        sent.head.hcursor.downField("text").as[String],
        Right(Notifier.formatMediaError(message, mediaFile, error))
      )
    }
  }

  test("mediaError does not send the errors of a file that does not exist") {
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      _ <- new TelegramNotifier[cats.effect.IO](opsChatId)(using summon, api)
        .mediaError(message, mediaFile, RepositoryError.NoResourcesFoundFile(mediaFile))
      calls <- recorded
    } yield assertEquals(sentMessages(calls), List.empty)
  }

  test("the default chat of the operators is the one the notifications always went to") {
    assertEquals(TelegramNotifier.defaultOpsChatId, -4145546019L)
  }
}
