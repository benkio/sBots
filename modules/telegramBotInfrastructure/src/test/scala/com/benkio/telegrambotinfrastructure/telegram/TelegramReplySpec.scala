package com.benkio.telegrambotinfrastructure.telegram

import cats.effect.IO
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.mocks.ResourceAccessMock
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

class TelegramReplySpec extends CatsEffectSuite {
  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  test("TelegramReply[Text] reply should work as expected") {
    val message = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some("test message"))
    val text    = Text("input Text")
    val result = TelegramReply.telegramTextReply
      .reply[IO](
        text,
        message,
        null,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendMessage reply")))
  }

  test("TelegramReply[VideoFile] reply should work as expected") {
    val message = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some("test message"))
    val video   = VideoFile("testVideo.mp4")
    val resourceAccessMock = ResourceAccessMock(
      getResourceFileHandler = mediaFile =>
        IO.raiseUnless(mediaFile.filepath == video.filepath)(
          Throwable(s"[resourceAccessMock] ${mediaFile.filepath} ≠ ${video.filepath}")
        ).as(MediaResource.MediaResourceIFile("test value"))
    )
    val result = TelegramReply.telegramVideoReply
      .reply[IO](
        video,
        message,
        resourceAccessMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendVideo reply")))
  }

  test("TelegramReply[PhotoFile] reply should work as expected") {
    val message = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some("test message"))
    val photo   = PhotoFile("testPhoto.jpg")
    val resourceAccessMock = ResourceAccessMock(
      getResourceFileHandler = mediaFile =>
        IO.raiseUnless(mediaFile.filepath == photo.filepath)(
          Throwable(s"[resourceAccessMock] ${mediaFile.filepath} ≠ ${photo.filepath}")
        ).as(MediaResource.MediaResourceIFile("test value"))
    )
    val result = TelegramReply.telegramPhotoReply
      .reply[IO](
        photo,
        message,
        resourceAccessMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendPhoto reply")))
  }

  test("TelegramReply[Document] reply should work as expected") {
    val message  = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some("test message"))
    val document = Document("testDocument.jpg")
    val resourceAccessMock = ResourceAccessMock(
      getResourceFileHandler = mediaFile =>
        IO.raiseUnless(mediaFile.filepath == document.filepath)(
          Throwable(s"[resourceAccessMock] ${mediaFile.filepath} ≠ ${document.filepath}")
        ).as(MediaResource.MediaResourceIFile("test value"))
    )
    val result = TelegramReply.telegramDocumentReply
      .reply[IO](
        document,
        message,
        resourceAccessMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendDocument reply")))
  }

  test("TelegramReply[GifFile] reply should work as expected") {
    val message = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some("test message"))
    val gif     = GifFile("testGif.mp4")
    val resourceAccessMock = ResourceAccessMock(
      getResourceFileHandler = mediaFile =>
        IO.raiseUnless(mediaFile.filepath == gif.filepath)(
          Throwable(s"[resourceAccessMock] ${mediaFile.filepath} ≠ ${gif.filepath}")
        ).as(MediaResource.MediaResourceIFile("test value"))
    )
    val result = TelegramReply.telegramGifReply
      .reply[IO](
        gif,
        message,
        resourceAccessMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendGif reply")))
  }

  test("TelegramReply[Mp3File] reply should work as expected") {
    val message = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some("test message"))
    val mp3     = Mp3File("testMp3.mp3")
    val resourceAccessMock = ResourceAccessMock(
      getResourceFileHandler = mediaFile =>
        IO.raiseUnless(mediaFile.filepath == mp3.filepath)(
          Throwable(s"[resourceAccessMock] ${mediaFile.filepath} ≠ ${mp3.filepath}")
        ).as(MediaResource.MediaResourceIFile("test value"))
    )
    val result = TelegramReply.telegramMp3Reply
      .reply[IO](
        mp3,
        message,
        resourceAccessMock,
        false
      )
      .map(messages => messages.map(_.text))
    assertIO(result, List(Some("[apiMock] sendMp3 reply")))
  }
}
