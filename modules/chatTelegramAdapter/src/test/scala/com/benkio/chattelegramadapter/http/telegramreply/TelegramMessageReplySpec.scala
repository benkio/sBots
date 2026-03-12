package com.benkio.chattelegramadapter.http.telegramreply

import cats.data.NonEmptyList
import cats.effect.IO
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.reply.Document
import com.benkio.chatcore.model.reply.GifFile
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.PhotoFile
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.reply.VideoFile
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.Logger.given
import com.benkio.chattelegramadapter.conversions.MessageConversions.*
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.MediaFileReply
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.TextReply
import com.benkio.chattelegramadapter.mocks.ApiMock.given
import munit.*

import scala.concurrent.duration.*

class TelegramMessageReplySpec extends CatsEffectSuite {

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
    val message        = Message(0, date = 0, chatId = ChatId(0), chatType = "private", text = Some("test message"))
    val video          = VideoFile("testVideo.mp4")
    val repositoryMock = RepositoryMock(
      getResourceFileHandler = mediaFile =>
        IO.raiseUnless(mediaFile.filepath == video.filepath)(
          Throwable(s"[repositoryMock] ${mediaFile.filepath} ≠ ${video.filepath}")
        ).as(Right(NonEmptyList.one(MediaResource.MediaResourceIFile("test value"))))
    )
    val result = MediaFileReply
      .sendVideo[IO](
        video,
        message.toTelegramium,
        repositoryMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendVideo reply")))
  }

  test("MediaFileReply.sendPhoto reply should work as expected") {
    val message        = Message(0, date = 0, chatId = ChatId(0), chatType = "private", text = Some("test message"))
    val photo          = PhotoFile("testPhoto.jpg")
    val repositoryMock = RepositoryMock(
      getResourceFileHandler = mediaFile =>
        IO.raiseUnless(mediaFile.filepath == photo.filepath)(
          Throwable(s"[repositoryMock] ${mediaFile.filepath} ≠ ${photo.filepath}")
        ).as(Right(NonEmptyList.one(MediaResource.MediaResourceIFile("test value"))))
    )
    val result = MediaFileReply
      .sendPhoto[IO](
        photo,
        message.toTelegramium,
        repositoryMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendPhoto reply")))
  }

  test("MediaFileReply.sendDocument reply should work as expected") {
    val message        = Message(0, date = 0, chatId = ChatId(0), chatType = "private", text = Some("test message"))
    val document       = Document("testDocument.jpg")
    val repositoryMock = RepositoryMock(
      getResourceFileHandler = mediaFile =>
        IO.raiseUnless(mediaFile.filepath == document.filepath)(
          Throwable(s"[repositoryMock] ${mediaFile.filepath} ≠ ${document.filepath}")
        ).as(Right(NonEmptyList.one(MediaResource.MediaResourceIFile("test value"))))
    )
    val result = MediaFileReply
      .sendDocument[IO](
        document,
        message.toTelegramium,
        repositoryMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendDocument reply")))
  }

  test("MediaFileReply.sendGif reply should work as expected") {
    val message        = Message(0, date = 0, chatId = ChatId(0), chatType = "private", text = Some("test message"))
    val gif            = GifFile("testGif.mp4")
    val repositoryMock = RepositoryMock(
      getResourceFileHandler = mediaFile =>
        IO.raiseUnless(mediaFile.filepath == gif.filepath)(
          Throwable(s"[repositoryMock] ${mediaFile.filepath} ≠ ${gif.filepath}")
        ).as(Right(NonEmptyList.one(MediaResource.MediaResourceIFile("test value"))))
    )
    val result = MediaFileReply
      .sendGif[IO](
        gif,
        message.toTelegramium,
        repositoryMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendGif reply")))
  }

  test("MediaFileReply.sendMp3 reply should work as expected") {
    val message        = Message(0, date = 0, chatId = ChatId(0), chatType = "private", text = Some("test message"))
    val mp3            = Mp3File("testMp3.mp3")
    val repositoryMock = RepositoryMock(
      getResourceFileHandler = mediaFile =>
        IO.raiseUnless(mediaFile.filepath == mp3.filepath)(
          Throwable(s"[repositoryMock] ${mediaFile.filepath} ≠ ${mp3.filepath}")
        ).as(Right(NonEmptyList.one(MediaResource.MediaResourceIFile("test value"))))
    )
    val result = MediaFileReply
      .sendMp3[IO](
        mp3,
        message.toTelegramium,
        repositoryMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendMp3 reply")))
  }

  test("TextReply.deleteMessage reply should work as expected") {
    val text   = Text("input Text with time to live")
    val result = TextReply
      .deleteMessage[IO](
        chatId = 0L,
        messageId = 0,
        ttl = 300.millis,
        reply = text
      )
    assertIO(result, true)
  }
}
