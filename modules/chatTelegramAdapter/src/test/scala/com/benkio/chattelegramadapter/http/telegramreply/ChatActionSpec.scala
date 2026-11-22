package com.benkio.chattelegramadapter.http.telegramreply

import cats.data.NonEmptyList
import cats.effect.IO
import com.benkio.chatcore.mocks.NotifierMock.given
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.media.MediaSource
import com.benkio.chatcore.model.reply.Document
import com.benkio.chatcore.model.reply.GifFile
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.PhotoFile
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.TelegramSticker
import com.benkio.chatcore.model.reply.VideoFile
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.Logger.given
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.TelegramMessageReply
import com.benkio.chattelegramadapter.mocks.PayloadRecordingApi
import com.benkio.chattelegramadapter.model.TelegramIds
import io.circe.Json
import munit.CatsEffectSuite

/** The chat action Telegram shows while a file is being sent matches the kind of the file. */
class ChatActionSpec extends CatsEffectSuite {

  private val msg: Message = Message(None, 0L, TelegramIds.chatId(10L), "private", text = Some("hi"))

  private val repository = RepositoryMock(getResourceFileHandler =
    _ =>
      IO.pure(
        Right(NonEmptyList.one(MediaResource.MediaResourceIFile(MediaSource.FileRef(Platform.Telegram, "a file id"))))
      )
  )

  private val expectedActions: List[(String, ReplyValue, String)] = List(
    ("an audio file", Mp3File("a.mp3"), "upload_voice"),
    ("a gif", GifFile("a.mp4"), "upload_document"),
    ("a photo", PhotoFile("a.jpg"), "upload_photo"),
    ("a video", VideoFile("a.mp4"), "upload_video"),
    ("a document", Document("a.pdf"), "upload_document"),
    ("a sticker", TelegramSticker("a.webp"), "choose_sticker")
  )

  private def chatActions(value: ReplyValue): IO[List[String]] =
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      _ <- TelegramMessageReply
        .sendReplyValue[IO](value, msg, repository, replyToMessage = false)(using summon, summon, api, summon)
      calls <- recorded
    } yield calls.collect { case ("sendChatAction", payload: Json) =>
      payload.hcursor.get[String]("action").getOrElse("")
    }

  expectedActions.foreach { case (name, value, action) =>
    test(s"$name shows the $action chat action") {
      chatActions(value).map(actions => assertEquals(actions, List(action)))
    }
  }
}
