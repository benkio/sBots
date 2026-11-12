package com.benkio.chattelegramadapter

import cats.data.NonEmptyList
import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.mocks.BackgroundJobManagerMock
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.NotifierMock.given
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.media.MediaResource.MediaResourceIFile
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.Markdown
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.PagedList
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.Logger.given
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.KeyboardReply
import com.benkio.chattelegramadapter.mocks.PayloadRecordingApi
import com.benkio.chattelegramadapter.mocks.SampleWebhookBot
import com.benkio.chattelegramadapter.model.TelegramCapabilities
import com.benkio.chattelegramadapter.model.TelegramIds
import com.benkio.chattelegramadapter.model.TelegramInlineKeyboard
import io.circe.Json
import munit.CatsEffectSuite
import telegramium.bots.high.Api

class TelegramPlatformAdapterSpec extends CatsEffectSuite {

  private val message: Message =
    Message(messageId = Some(TelegramIds.messageId(7)), date = 0L, chatId = TelegramIds.chatId(0L), chatType = "test")

  private def adapter(api: Api[IO], startUpdates: IO[Unit] = IO.unit): TelegramPlatformAdapter[IO] =
    new TelegramPlatformAdapter[IO](
      repository =
        RepositoryMock((_, _) => NonEmptyList.one(NonEmptyList.one(MediaResourceIFile("not used"))).pure[IO]),
      backgroundJobManager = BackgroundJobManagerMock.mock(),
      effectfulCallbacks = Map.empty,
      dbLayer = DBLayerMock.mock(SampleWebhookBot.sBotInfo.botId),
      ttl = None,
      startUpdates = startUpdates
    )(using summon, summon, api)

  private def callsOf(method: String, calls: List[(String, Json)]): List[Json] =
    calls.collect { case (name, payload) if name == method => payload }

  test("the platform of the adapter is Telegram, with the Telegram capabilities") {
    PayloadRecordingApi().map { case (api, _) =>
      assertEquals(adapter(api).platform, Platform.Telegram)
      assertEquals(adapter(api).capabilities, TelegramCapabilities.value)
    }
  }

  test("run starts the delivery of the updates") {
    for {
      apiAndRecording <- PayloadRecordingApi()
      started         <- Ref.of[IO, Int](0)
      _               <- adapter(apiAndRecording._1, startUpdates = started.update(_ + 1)).run
      count           <- started.get
    } yield assertEquals(count, 1)
  }

  test("send should send a text through the Telegram API") {
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      _     <- adapter(api).send(Text("plain"), message, replyToMessage = false)
      calls <- recorded
    } yield assertEquals(callsOf("sendMessage", calls).size, 1)
  }

  test("send should reply to the message only when asked to") {
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      _     <- adapter(api).send(Text("plain"), message, replyToMessage = false)
      _     <- adapter(api).send(Text("plain"), message, replyToMessage = true)
      calls <- recorded
    } yield {
      val sendMessages = callsOf("sendMessage", calls)
      assertEquals(sendMessages.size, 2)
      assert(sendMessages.head.hcursor.downField("reply_parameters").failed, sendMessages.head.noSpaces)
      assert(sendMessages.last.hcursor.downField("reply_parameters").succeeded, sendMessages.last.noSpaces)
    }
  }

  test("send should send a markdown as a rich message") {
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      _     <- adapter(api).send(Markdown("**bold**"), message, replyToMessage = false)
      calls <- recorded
    } yield assertEquals(callsOf("sendRichMessage", calls).size, 1)
  }

  test("runEffectful should compute the value of the effectful key, which send then delivers") {
    val effectfulReply = EffectfulReply(EffectfulKey.Subscriptions(SampleWebhookBot.sBotInfo))
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      value <- adapter(api).runEffectful(effectfulReply, message)
      _     <- adapter(api).send(value, message, replyToMessage = false)
      calls <- recorded
    } yield {
      assert(value.isInstanceOf[Markdown], value.toString)
      assertEquals(callsOf("sendRichMessage", calls).size, 1)
    }
  }

  test("send should send a paged list as the keyboard Telegram always sent for it") {
    val medias: List[MediaFile] = (1 to 7).toList.map(n => Mp3File(s"m$n.mp3"))
    val pagedList               = PagedList.topTwenty(medias, page = 1)
    val keyboard                = TelegramInlineKeyboard(
      keyboardTitle = "-----Top 20 Triggers-----",
      inlineKeyboard = KeyboardReply.buildInlineKeyboard(data = medias, page = 1, commandKey = CommandKey.TopTwenty)
    )
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      _     <- adapter(api).send(pagedList, message, replyToMessage = false)
      _     <- adapter(api).send(keyboard, message, replyToMessage = false)
      calls <- recorded
    } yield {
      val sendMessages = callsOf("sendMessage", calls)
      assertEquals(sendMessages.size, 2)
      assertEquals(sendMessages.head, sendMessages.last)
      assert(sendMessages.head.hcursor.downField("reply_markup").succeeded, sendMessages.head.noSpaces)
    }
  }

  test("runEffectful of the commands that list gives a paged list, which send delivers") {
    val topTwenty = EffectfulReply(EffectfulKey.TopTwenty(SampleWebhookBot.sBotInfo))
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      value <- adapter(api).runEffectful(topTwenty, message)
      _     <- adapter(api).send(value, message, replyToMessage = false)
      calls <- recorded
    } yield {
      assert(value.isInstanceOf[PagedList], value.toString)
      assertEquals(value.asInstanceOf[PagedList].commandKey, CommandKey.TopTwenty)
      assertEquals(callsOf("sendMessage", calls).size, 1)
    }
  }
}
