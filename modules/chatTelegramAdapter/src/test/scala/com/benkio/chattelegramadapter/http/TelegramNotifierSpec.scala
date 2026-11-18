package com.benkio.chattelegramadapter.http

import cats.effect.IO
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.Repository.RepositoryError
import com.benkio.chatcore.Logger.given
import com.benkio.chatcore.Notifier
import com.benkio.chattelegramadapter.mocks.PayloadRecordingApi
import com.benkio.chattelegramadapter.model.TelegramIds
import io.circe.Json
import munit.CatsEffectSuite
import telegramium.bots.high.Api

class TelegramNotifierSpec extends CatsEffectSuite {

  private val opsChatId: Long = -1001L
  private val message         =
    Message(Some(TelegramIds.messageId(1)), 0L, TelegramIds.chatId(5L), "private", text = Some("hello"))
  private val mediaFile = Mp3File("audio.mp3")
  private val botInfo   = SBotInfo(SBotInfo.SBotId("bot"), SBotInfo.SBotName("BotName"))

  // The notifier over the recording api of the test: the only place that spells out the given arguments
  private def notifierOver(api: Api[IO]): TelegramNotifier[IO] =
    new TelegramNotifier[IO](opsChatId)(using summon, api, summon)

  private def sentMessages(calls: List[(String, Json)]): List[Json] =
    calls.collect { case ("sendMessage", payload) => payload }

  test("info sends the message to the chat of the operators") {
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      _     <- notifierOver(api).info(botInfo, "started")
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
      _ <- notifierOver(api)
        .mediaError(message, mediaFile, error)
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
      _ <- notifierOver(api)
        .mediaError(message, mediaFile, RepositoryError.NoResourcesFoundFile(mediaFile))
      calls <- recorded
    } yield assertEquals(sentMessages(calls), List.empty)
  }

  private def failedRequest(code: Int, description: String): Throwable =
    telegramium.bots.high.FailedRequest(
      method = telegramium.bots.high.Methods.sendAnimation(
        chatId = telegramium.bots.ChatIntId(5L),
        animation = telegramium.bots.InputLinkFile("https://example.com/a.mp4")
      ),
      errorCode = Some(code),
      description = Some(description)
    )

  test("mediaError does not send the 400 of a chat where the bot has no rights") {
    val error = failedRequest(400, "Bad Request: not enough rights to send animations to the chat")
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      _ <- notifierOver(api)
        .mediaError(message, mediaFile, error)
      calls <- recorded
    } yield {
      assertEquals(
        error.getMessage,
        "method=sendAnimation code=400 description=Bad Request: not enough rights to send animations to the chat"
      )
      assertEquals(sentMessages(calls), List.empty)
    }
  }

  test("mediaError still sends the other 400s") {
    val error = failedRequest(400, "Bad Request: wrong file identifier")
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      _ <- notifierOver(api)
        .mediaError(message, mediaFile, error)
      calls <- recorded
    } yield assertEquals(sentMessages(calls).size, 1)
  }

  test("error sends what the bot was doing and the error to the chat of the operators") {
    val error = new RuntimeException("boom")
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      _     <- notifierOver(api).error(botInfo, "the cron job", error)
      calls <- recorded
    } yield {
      val sent = sentMessages(calls)
      assertEquals(sent.size, 1)
      assertEquals(sent.head.hcursor.downField("chat_id").as[Long], Right(opsChatId))
      assertEquals(
        sent.head.hcursor.downField("text").as[String],
        Right(Notifier.formatError(botInfo, "the cron job", error))
      )
    }
  }

  test("error does not send the 400 of a chat where the bot has no rights, nor an error of a missing file") {
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      notifier        = notifierOver(api)
      _ <- notifier.error(
        botInfo,
        "a reply",
        new RuntimeException(
          "method=sendAnimation code=400 description=Bad Request: not enough rights to send animations"
        )
      )
      _     <- notifier.error(botInfo, "a reply", RepositoryError.NoResourcesFoundFile(mediaFile))
      calls <- recorded
    } yield assertEquals(sentMessages(calls), List.empty[Json])
  }
}
