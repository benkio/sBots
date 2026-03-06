package com.benkio.telegrambotinfrastructure.model.reply

import cats.data.NonEmptyList
import cats.effect.*
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.mocks.BackgroundJobManagerMock
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.mocks.RepositoryMock
import com.benkio.telegrambotinfrastructure.mocks.SampleWebhookBot
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceIFile
import com.benkio.telegrambotinfrastructure.model.ChatId
import com.benkio.telegrambotinfrastructure.model.Message
import com.benkio.telegrambotinfrastructure.model.StringTextTriggerValue
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import com.benkio.telegrambotinfrastructure.ComputeReply
import com.benkio.telegrambotinfrastructure.Logger.given
import munit.CatsEffectSuite

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
      chatType = "test",
      text = None,
      caption = None,
      newChatMembers = List.empty,
      leftChatMember = None,
      forwardOrigin = None
    )

    def computeResult(input: ReplyBundleMessage): IO[List[Message]] =
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

    val result: IO[List[Message]] =
      computeResult(replyBundleInput)

    result.map(r => {
      assertEquals(r.length, 1)
      assertEquals(r.map(_.text), List(Some("[apiMock] sendMessage reply")))
    })
  }
}
