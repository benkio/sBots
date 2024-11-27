package com.benkio.telegrambotinfrastructure.telegram

import com.benkio.telegrambotinfrastructure.mocks.ResourceAccessMock
import telegramium.bots.high.Api
import com.benkio.telegrambotinfrastructure.mocks.ApiMock
import log.effect.LogLevels
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogWriter
import cats.effect.IO
import com.benkio.telegrambotinfrastructure.model.*
import telegramium.bots.Chat
import telegramium.bots.Message

import munit._

class TelegramReplySpec extends CatsEffectSuite {
  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)
  given Api[IO]            = new ApiMock

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
    val video    = VideoFile("testVideo.mp4")
    val resourceAccessMock = ResourceAccessMock(
      getResourceByteArrayHandler = (filepath) =>
      IO.raiseUnless(filepath == video.filepath)(Throwable(s"[resourceAccessMock] $filepath ≠ ${video.filepath}")).as(Array.fill(20)((scala.util.Random.nextInt(256) - 128).toByte))
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
    val photo    = PhotoFile("testPhoto.jpg")
    val resourceAccessMock = ResourceAccessMock(
      getResourceByteArrayHandler = (filepath) =>
      IO.raiseUnless(filepath == photo.filepath)(Throwable(s"[resourceAccessMock] $filepath ≠ ${photo.filepath}")).as(Array.fill(20)((scala.util.Random.nextInt(256) - 128).toByte))
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

  test("TelegramReply[GifFile] reply should work as expected") {
    val message = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some("test message"))
    val gif    = GifFile("testGif.mp4")
    val resourceAccessMock = ResourceAccessMock(
      getResourceByteArrayHandler = (filepath) =>
      IO.raiseUnless(filepath == gif.filepath)(Throwable(s"[resourceAccessMock] $filepath ≠ ${gif.filepath}")).as(Array.fill(20)((scala.util.Random.nextInt(256) - 128).toByte))
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
    val mp3    = Mp3File("testMp3.mp3")
    val resourceAccessMock = ResourceAccessMock(
      getResourceByteArrayHandler = (filepath) =>
      IO.raiseUnless(filepath == mp3.filepath)(Throwable(s"[resourceAccessMock] $filepath ≠ ${mp3.filepath}")).as(Array.fill(20)((scala.util.Random.nextInt(256) - 128).toByte))
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
