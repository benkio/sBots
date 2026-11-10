package com.benkio.chattelegramadapter.model.reply

import cats.data.NonEmptyList
import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.messagefiltering.MessageMatches
import com.benkio.chatcore.mocks.BackgroundJobManagerMock
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.media.MediaResource.MediaResourceIFile
import com.benkio.chatcore.model.reply.Document
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.GifFile
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.PhotoFile
import com.benkio.chatcore.model.reply.PlainReply
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.reply.VideoFile
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandTrigger
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.StringTextTriggerValue
import com.benkio.chatcore.model.TextTrigger
import com.benkio.chatcore.Logger.given
import com.benkio.chattelegramadapter.mocks.ApiMock.given
import com.benkio.chattelegramadapter.mocks.PayloadRecordingApi
import com.benkio.chattelegramadapter.mocks.SampleWebhookBot
import com.benkio.chattelegramadapter.ComputeReply
import munit.CatsEffectSuite
import telegramium.bots.Message as TMessage

class ComputeReplySpec extends CatsEffectSuite {

  val inputMediafile: List[MediaFile] = List(
    Mp3File("audio.mp3"),
    PhotoFile("picture.jpg"),
    PhotoFile("picture.png"),
    GifFile("aGif.mp4"),
    VideoFile("video.mp4"),
    Document("document.pdf")
  )

  test("ComputeReply.execute should return the expected message when the ReplyBundle and Message is provided") {

    def input(reply: PlainReply): ReplyBundleMessage =
      ReplyBundleMessage(
        trigger = TextTrigger(
          StringTextTriggerValue("test")
        ),
        reply = reply,
        matcher = MessageMatches.ContainsOnce
      )

    val replyBundleInput: ReplyBundleMessage = input(
      TextReply.fromList(
        "Reply sent to Telegram"
      )(false)
    )

    val message: Message = Message(
      messageId = 0,
      date = 0L,
      chatId = ChatId(0L),
      chatType = "test"
    )

    def computeResult(input: ReplyBundleMessage): IO[List[TMessage]] =
      ComputeReply.execute(
        replyBundle = input,
        message = message,
        ttl = None,
        repository =
          RepositoryMock((_, _) => NonEmptyList.one(NonEmptyList.one(MediaResourceIFile("not used"))).pure[IO]),
        backgroundJobManager = BackgroundJobManagerMock.mock(),
        effectfulCallbacks = Map.empty,
        dbLayer = DBLayerMock.mock(SampleWebhookBot.sBotInfo.botId)
      )

    val result: IO[List[TMessage]] =
      computeResult(replyBundleInput)

    result.map(r => {
      assertEquals(r.length, 1)
      assertEquals(r.map(_.text), List(Some("[apiMock] sendMessage reply")))
    })
  }

  private val message: Message = Message(messageId = 0, date = 0L, chatId = ChatId(0L), chatType = "test")
  private val repository       =
    RepositoryMock((_, _) => NonEmptyList.one(NonEmptyList.one(MediaResourceIFile("not used"))).pure[IO])
  private val dbLayer = DBLayerMock.mock(SampleWebhookBot.sBotInfo.botId)

  private val effectfulReply   = EffectfulReply(EffectfulKey.Subscriptions(SampleWebhookBot.sBotInfo))
  private val effectfulCommand = ReplyBundleCommand(
    trigger = CommandTrigger("subscriptions"),
    reply = effectfulReply,
    instruction = CommandInstructionData.NoInstructions
  )

  test("ComputeReply.execute should run an effectful reply of a command bundle") {
    ComputeReply
      .execute(
        replyBundle = effectfulCommand,
        message = message,
        repository = repository,
        backgroundJobManager = BackgroundJobManagerMock.mock(),
        effectfulCallbacks = Map.empty,
        dbLayer = dbLayer,
        ttl = None
      )
      .map(r => assertEquals(r.map(_.text), List(Some("[apiMock] sendRichMessage reply"))))
  }

  test("ComputeReply.executePlain should reply with a text without any effectful dependency") {
    ComputeReply
      .executePlain(reply = TextReply.fromList("plain")(false), message = message, repository = repository)
      .map(r => assertEquals(r.map(_.text), List(Some("[apiMock] sendMessage reply"))))
  }

  test("ComputeReply.executePlain should override the replyToMessage of the reply") {
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      _ <- ComputeReply.executePlain(
        reply = TextReply.fromList("plain")(true),
        message = message.copy(messageId = 7),
        repository = repository,
        replyToMessage = Some(false)
      )(using summon, summon, api)
      _ <- ComputeReply.executePlain(
        reply = TextReply.fromList("plain")(true),
        message = message.copy(messageId = 7),
        repository = repository
      )(using summon, summon, api)
      calls <- recorded
    } yield {
      val sendMessages = calls.collect { case ("sendMessage", payload) => payload }
      assertEquals(sendMessages.size, 2)
      assert(sendMessages.head.hcursor.downField("reply_parameters").failed, sendMessages.head.noSpaces)
      assert(sendMessages.last.hcursor.downField("reply_parameters").succeeded, sendMessages.last.noSpaces)
    }
  }

  test("ComputeReply.executeEffectful should run the effectful key") {
    ComputeReply
      .executeEffectful(
        reply = effectfulReply,
        message = message,
        repository = repository,
        backgroundJobManager = BackgroundJobManagerMock.mock(),
        effectfulCallbacks = Map.empty,
        dbLayer = dbLayer,
        ttl = None
      )
      .map(r => assertEquals(r.map(_.text), List(Some("[apiMock] sendRichMessage reply"))))
  }

  test("ComputeReply.runPlainReply should pick one of the values of the reply") {
    ComputeReply
      .runPlainReply[IO](TextReply.fromList("a", "b")(false))
      .map(value => assert(Set("a", "b").contains(value.show.trim), value.show))
  }
}
