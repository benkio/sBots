package com.benkio.chatcore.model.reply

import cats.data.NonEmptyList
import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.messagefiltering.MessageMatches
import com.benkio.chatcore.mocks.ApiMock.given
import com.benkio.chatcore.mocks.BackgroundJobManagerMock
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.mocks.SampleWebhookBot
import com.benkio.chatcore.model.media.MediaResource.MediaResourceIFile
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.StringTextTriggerValue
import com.benkio.chatcore.model.TextTrigger
import com.benkio.chatcore.Logger.given
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

    def input(reply: Reply): ReplyBundleMessage =
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
}
