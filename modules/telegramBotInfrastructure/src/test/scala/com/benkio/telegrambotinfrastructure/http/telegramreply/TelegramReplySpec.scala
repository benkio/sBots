package com.benkio.telegrambotinfrastructure.telegram.telegramreply

import cats.data.NonEmptyList
import cats.effect.IO

import com.benkio.telegrambotinfrastructure.http.telegramreply.MediaFileReply
import com.benkio.telegrambotinfrastructure.http.telegramreply.TextReply
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.mocks.RepositoryMock
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.model.reply.Document
import com.benkio.telegrambotinfrastructure.model.reply.GifFile
import com.benkio.telegrambotinfrastructure.model.reply.Mp3File
import com.benkio.telegrambotinfrastructure.model.reply.PhotoFile
import com.benkio.telegrambotinfrastructure.model.reply.Text
import com.benkio.telegrambotinfrastructure.model.reply.VideoFile
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.*
import telegramium.bots.Chat
import telegramium.bots.Message

import scala.concurrent.duration.*

class TelegramReplySpec extends CatsEffectSuite {
  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  test("TextReply.sendText reply should work as expected") {
    val message = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some("test message"))
    val text    = Text("input Text")
    val result  = TextReply
      .sendText[IO](
        text,
        message,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendMessage reply")))
  }

  test("MediaFileReply.sendVideo reply should work as expected") {
    val message        = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some("test message"))
    val video          = VideoFile("testVideo.mp4")
    val repositoryMock = RepositoryMock(
      getResourceFileHandler = mediaFile =>
        IO.raiseUnless(mediaFile.filepath == video.filepath)(
          Throwable(s"[repositoryMock] ${mediaFile.filepath} ≠ ${video.filepath}")
        ).as(NonEmptyList.one(MediaResource.MediaResourceIFile("test value")))
    )
    val result = MediaFileReply
      .sendVideo[IO](
        video,
        message,
        repositoryMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendVideo reply")))
  }

  test("MediaFileReply.sendPhoto reply should work as expected") {
    val message        = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some("test message"))
    val photo          = PhotoFile("testPhoto.jpg")
    val repositoryMock = RepositoryMock(
      getResourceFileHandler = mediaFile =>
        IO.raiseUnless(mediaFile.filepath == photo.filepath)(
          Throwable(s"[repositoryMock] ${mediaFile.filepath} ≠ ${photo.filepath}")
        ).as(NonEmptyList.one(MediaResource.MediaResourceIFile("test value")))
    )
    val result = MediaFileReply
      .sendPhoto[IO](
        photo,
        message,
        repositoryMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendPhoto reply")))
  }

  test("MediaFileReply.sendDocument reply should work as expected") {
    val message        = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some("test message"))
    val document       = Document("testDocument.jpg")
    val repositoryMock = RepositoryMock(
      getResourceFileHandler = mediaFile =>
        IO.raiseUnless(mediaFile.filepath == document.filepath)(
          Throwable(s"[repositoryMock] ${mediaFile.filepath} ≠ ${document.filepath}")
        ).as(NonEmptyList.one(MediaResource.MediaResourceIFile("test value")))
    )
    val result = MediaFileReply
      .sendDocument[IO](
        document,
        message,
        repositoryMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendDocument reply")))
  }

  test("MediaFileReply.sendGif reply should work as expected") {
    val message        = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some("test message"))
    val gif            = GifFile("testGif.mp4")
    val repositoryMock = RepositoryMock(
      getResourceFileHandler = mediaFile =>
        IO.raiseUnless(mediaFile.filepath == gif.filepath)(
          Throwable(s"[repositoryMock] ${mediaFile.filepath} ≠ ${gif.filepath}")
        ).as(NonEmptyList.one(MediaResource.MediaResourceIFile("test value")))
    )
    val result = MediaFileReply
      .sendGif[IO](
        gif,
        message,
        repositoryMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendGif reply")))
  }

  test("MediaFileReply.sendMp3 reply should work as expected") {
    val message        = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some("test message"))
    val mp3            = Mp3File("testMp3.mp3")
    val repositoryMock = RepositoryMock(
      getResourceFileHandler = mediaFile =>
        IO.raiseUnless(mediaFile.filepath == mp3.filepath)(
          Throwable(s"[repositoryMock] ${mediaFile.filepath} ≠ ${mp3.filepath}")
        ).as(NonEmptyList.one(MediaResource.MediaResourceIFile("test value")))
    )
    val result = MediaFileReply
      .sendMp3[IO](
        mp3,
        message,
        repositoryMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendMp3 reply")))
  }

  test("TextReply.deleteMessage reply should work as expected") {
    val text    = Text("input Text with time to live")
    val result  = TextReply
      .deleteMessage[IO](
        chatId = 0L,
        messageId = 0,
        ttl = 300.millis,
        reply = text
      )
    assertIO(result, true)
  }
}
