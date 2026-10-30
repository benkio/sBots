package com.benkio.chattelegramadapter.http.telegramreply

import cats.data.NonEmptyList
import cats.effect.IO
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.reply.Document
import com.benkio.chatcore.model.reply.GifFile
import com.benkio.chatcore.model.reply.MarkdownFile
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.PhotoFile
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.reply.VideoFile
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.Logger.given
import com.benkio.chattelegramadapter.conversions.MessageConversions.*
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.MediaFileReply
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.TextReply
import com.benkio.chattelegramadapter.mocks.ApiMock.given
import munit.*

import scala.concurrent.duration.*

class TelegramMessageReplySpec extends CatsEffectSuite {

  private val testMessage: Message =
    Message(0, date = 0, chatId = ChatId(0), chatType = "private", text = Some("test message"))

  private def iFileRepositoryMock(expectedFilepath: String): RepositoryMock =
    RepositoryMock(
      getResourceFileHandler = mediaFile =>
        IO.raiseUnless(mediaFile.filepath == expectedFilepath)(
          Throwable(s"[repositoryMock] ${mediaFile.filepath} ≠ $expectedFilepath")
        ).as(Right(NonEmptyList.one(MediaResource.MediaResourceIFile("test value"))))
    )

  test("TextReply.sendText reply should work as expected") {
    val message: Message = Message(
      messageId = 0,
      date = 0L,
      chatId = ChatId(0L),
      chatType = "private",
      text = Some("test message"),
      caption = None
    )
    val text   = Text("input Text")
    val result = TextReply
      .sendText[IO](
        text,
        message,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendMessage reply")))
  }

  test("MediaFileReply.sendVideo reply should work as expected") {
    val video          = VideoFile("testVideo.mp4")
    val repositoryMock = iFileRepositoryMock(video.filepath)
    val result         = MediaFileReply
      .sendVideo[IO](
        video,
        testMessage.toTelegramium,
        repositoryMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendVideo reply")))
  }

  test("MediaFileReply.sendPhoto reply should work as expected") {
    val photo          = PhotoFile("testPhoto.jpg")
    val repositoryMock = iFileRepositoryMock(photo.filepath)
    val result         = MediaFileReply
      .sendPhoto[IO](
        photo,
        testMessage.toTelegramium,
        repositoryMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendPhoto reply")))
  }

  test("MediaFileReply.sendDocument reply should work as expected") {
    val document       = Document("testDocument.jpg")
    val repositoryMock = iFileRepositoryMock(document.filepath)
    val result         = MediaFileReply
      .sendDocument[IO](
        document,
        testMessage.toTelegramium,
        repositoryMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendDocument reply")))
  }

  test("MediaFileReply.sendGif reply should work as expected") {
    val gif            = GifFile("testGif.mp4")
    val repositoryMock = iFileRepositoryMock(gif.filepath)
    val result         = MediaFileReply
      .sendGif[IO](
        gif,
        testMessage.toTelegramium,
        repositoryMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendGif reply")))
  }

  test("MediaFileReply.sendMp3 reply should work as expected") {
    val mp3            = Mp3File("testMp3.mp3")
    val repositoryMock = iFileRepositoryMock(mp3.filepath)
    val result         = MediaFileReply
      .sendMp3[IO](
        mp3,
        testMessage.toTelegramium,
        repositoryMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendMp3 reply")))
  }

  test("MediaFileReply.sendMarkdown reply should work as expected") {
    val markdown       = MarkdownFile("testMarkdown.md")
    val markdownBody   = "# test markdown body"
    val repositoryMock = RepositoryMock(
      getResourceFileHandler = mediaFile =>
        IO.raiseUnless(mediaFile.filepath == markdown.filepath)(
          Throwable(s"[repositoryMock] ${mediaFile.filepath} ≠ ${markdown.filepath}")
        ).as(
          Right(
            NonEmptyList.one(
              MediaResource.MediaResourceFile(
                Repository.toTempFile[IO](markdown.filepath, markdownBody.getBytes())
              )
            )
          )
        )
    )

    val result = MediaFileReply
      .sendMarkdown[IO](
        markdown,
        testMessage.toTelegramium,
        repositoryMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendMessage reply")))
  }

  test("MediaFileReply.sendMarkdown should return empty when source is MediaResourceIFile") {
    val markdown       = MarkdownFile("testMarkdown.md")
    val repositoryMock = iFileRepositoryMock(markdown.filepath)

    val result = MediaFileReply.sendMarkdown[IO](
      markdown,
      testMessage.toTelegramium,
      repositoryMock,
      false
    )

    assertIO(result, List.empty)
  }

  test("TextReply.deleteMessage reply should work as expected") {
    val text   = Text("input Text with time to live")
    val result = TextReply
      .deleteMessage[IO](
        chatId = 0L,
        messageId = 0,
        ttl = 300.millis,
        value = text.value
      )
    assertIO(result, true)
  }
}
