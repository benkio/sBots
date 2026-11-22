package com.benkio.chattelegramadapter.http.telegramreply

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.Ref
import cats.syntax.all.*
import com.benkio.chatcore.mocks.NotifierMock.given
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.media.MediaSource
import com.benkio.chatcore.model.reply.Document
import com.benkio.chatcore.model.reply.GifFile
import com.benkio.chatcore.model.reply.Markdown
import com.benkio.chatcore.model.reply.MarkdownFile
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.PhotoFile
import com.benkio.chatcore.model.reply.TelegramSticker as ChatSticker
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.reply.VideoFile
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.Logger.given
import com.benkio.chattelegramadapter.conversions.MessageConversions.*
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.MediaFileReply
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.TelegramMessageReply
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.TextReply
import com.benkio.chattelegramadapter.mocks.ApiMock.given
import com.benkio.chattelegramadapter.model.TelegramIds
import munit.*
import telegramium.bots.Animation
import telegramium.bots.Audio
import telegramium.bots.Chat
import telegramium.bots.ChatId
import telegramium.bots.Document as TDocument
import telegramium.bots.IFile
import telegramium.bots.InputLinkFile
import telegramium.bots.Message as TMessage
import telegramium.bots.PhotoSize
import telegramium.bots.ReplyParameters
import telegramium.bots.Sticker as TSticker
import telegramium.bots.Video as TVideo

import scala.concurrent.duration.*

class TelegramMessageReplySpec extends CatsEffectSuite {

  private val testMessage: Message =
    Message(None, date = 0, chatId = TelegramIds.chatId(0), chatType = "private", text = Some("test message"))

  private def iFileRepositoryMock(expectedFilepath: String): RepositoryMock =
    RepositoryMock(
      getResourceFileHandler = mediaFile =>
        IO.raiseUnless(mediaFile.filepath == expectedFilepath)(
          Throwable(s"[repositoryMock] ${mediaFile.filepath} ≠ $expectedFilepath")
        ).as(
          Right(
            NonEmptyList.one(MediaResource.MediaResourceIFile(MediaSource.FileRef(Platform.Telegram, "test value")))
          )
        )
    )

  private def messageWithFileId(fileId: String, kind: String): TMessage = {
    val base = TMessage(0, date = 0, chat = Chat(0, "private"))
    kind match {
      case "audio"     => base.copy(audio = Some(Audio(fileId, "unique", 0)))
      case "animation" => base.copy(animation = Some(Animation(fileId, "unique", 0, 0, 0)))
      case "video"     => base.copy(video = Some(TVideo(fileId, "unique", 0, 0, 0)))
      case "document"  => base.copy(document = Some(TDocument(fileId, "unique")))
      case "sticker"   => base.copy(sticker = Some(TSticker(fileId, "unique", "regular", 0, 0, false, false)))
      case "photo"     =>
        base.copy(photo = List(PhotoSize("other", "other-unique", 0, 0), PhotoSize(fileId, "unique", 0, 0)))
      case other => throw new IllegalArgumentException(s"unknown media field: $other")
    }
  }

  private def sendPattern(
      mediaFile: MediaFile,
      repository: Repository[IO],
      sendFileAPIMethod: (ChatId, IFile, Option[ReplyParameters]) => IO[TMessage]
  ): IO[List[TMessage]] =
    TelegramMessageReply.telegramFileReplyPattern[IO](
      msg = testMessage.toTelegramium,
      repository = repository,
      chatAction = "upload_voice",
      mediaFile = mediaFile,
      replyToMessage = false,
      sendFileAPIMethod = sendFileAPIMethod
    )

  test("TextReply.sendText reply should work as expected") {
    val message: Message = Message(
      messageId = None,
      date = 0L,
      chatId = TelegramIds.chatId(0L),
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
    assertIO(result, List(Some("[apiMock] sendRichMessage reply")))
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

  test("TelegramMessageReply.sendReplyValue should route Markdown replies through rich text") {
    val markdown = Markdown("## test markdown body")
    val result   = TelegramMessageReply
      .sendReplyValue[IO](
        replyValue = markdown,
        msg = testMessage,
        repository = RepositoryMock(),
        replyToMessage = false
      )
      .map(messages => messages.map(_.text))

    assertIO(result, List(Some("[apiMock] sendRichMessage reply")))
  }

  test("TelegramMessageReply.sendReplyValue should support Markdown replies with ttl") {
    val markdown = Markdown("## test markdown body", Some(50.millis))
    val result   = TelegramMessageReply
      .sendReplyValue[IO](
        replyValue = markdown,
        msg = testMessage,
        repository = RepositoryMock(),
        replyToMessage = false
      )
      .map(messages => messages.map(_.text))

    assertIO(result, List(Some("[apiMock] sendRichMessage reply")))
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

  test("captures and persists the file ID returned for each media type with the Telegram tag") {
    val cases: List[(MediaFile, String, String)] = List(
      (Mp3File("f.mp3"), "audio", "audio-id"),
      (GifFile("fGif.mp4"), "animation", "animation-id"),
      (VideoFile("f.mp4"), "video", "video-id"),
      (Document("f.jpg"), "document", "document-id"),
      (ChatSticker("f.sticker"), "sticker", "sticker-id"),
      (PhotoFile("f.jpg"), "photo", "photo-id")
    )
    cases.traverse_ { case (mediaFile, kind, id) =>
      for {
        calls <- Ref.of[IO, List[(MediaFile, Platform, String)]](List.empty)
        repository = RepositoryMock(
          getResourceFileHandler = _ =>
            IO.pure(
              Right(
                NonEmptyList.one(MediaResource.MediaResourceIFile(MediaSource.FileRef(Platform.Telegram, "stored")))
              )
            ),
          registerFileIdHandler = (mf, platform, fileId) => calls.update(_ :+ ((mf, platform, fileId)))
        )
        response = messageWithFileId(id, kind)
        sent <- sendPattern(
          mediaFile = mediaFile,
          repository = repository,
          sendFileAPIMethod = (_, _, _) => IO.pure(response)
        )
        recorded <- calls.get
      } yield {
        assertEquals(sent, List(response))
        assertEquals(recorded, List((mediaFile, Platform.Telegram, id)))
      }
    }
  }

  test("replaces a failed stored reference with the fresh ID returned by the URL fallback") {
    val mp3   = Mp3File("fallback.mp3")
    val fresh = messageWithFileId("fresh-id", "audio")
    for {
      calls <- Ref.of[IO, List[(MediaFile, Platform, String, String)]](List.empty)
      repository = RepositoryMock(
        getResourceFileHandler = _ =>
          IO.pure(
            Right(
              NonEmptyList.of(
                MediaResource.MediaResourceIFile(MediaSource.FileRef(Platform.Telegram, "bad-id")),
                MediaResource.MediaResourceFile(Repository.toTempFile[IO]("fallback.mp3", "content".getBytes()))
              )
            )
          ),
        replaceFileIdHandler = (mf, platform, failedId, newId) => calls.update(_ :+ ((mf, platform, failedId, newId)))
      )
      sent <- sendPattern(
        mediaFile = mp3,
        repository = repository,
        sendFileAPIMethod = (_, ifile, _) =>
          ifile match {
            case InputLinkFile(id) => IO.raiseError(new RuntimeException(s"stale reference $id"))
            case _                 => IO.pure(fresh)
          }
      )
      recorded <- calls.get
    } yield {
      assertEquals(sent, List(fresh))
      assertEquals(recorded, List((mp3, Platform.Telegram, "bad-id", "fresh-id")))
    }
  }

  test("a send by a stored reference that returns the same file ID does not touch the repository") {
    val mp3      = Mp3File("f.mp3")
    val response = messageWithFileId("stored", "audio")
    for {
      calls <- Ref.of[IO, Int](0)
      repository = RepositoryMock(
        getResourceFileHandler = _ =>
          IO.pure(
            Right(NonEmptyList.one(MediaResource.MediaResourceIFile(MediaSource.FileRef(Platform.Telegram, "stored"))))
          ),
        registerFileIdHandler = (_, _, _) => calls.update(_ + 1),
        replaceFileIdHandler = (_, _, _, _) => calls.update(_ + 1)
      )
      sent <- sendPattern(mediaFile = mp3, repository = repository, sendFileAPIMethod = (_, _, _) => IO.pure(response))
      writes <- calls.get
    } yield {
      assertEquals(sent, List(response))
      assertEquals(writes, 0)
    }
  }

  test("a failing persistence does not change the sent messages") {
    val mp3      = Mp3File("f.mp3")
    val response = messageWithFileId("id", "audio")
    for {
      calls <- Ref.of[IO, Int](0)
      repository = RepositoryMock(
        getResourceFileHandler = _ =>
          IO.pure(
            Right(NonEmptyList.one(MediaResource.MediaResourceIFile(MediaSource.FileRef(Platform.Telegram, "stored"))))
          ),
        registerFileIdHandler = (_, _, _) => calls.update(_ + 1) >> IO.raiseError(new RuntimeException("db down"))
      )
      sent <- sendPattern(mediaFile = mp3, repository = repository, sendFileAPIMethod = (_, _, _) => IO.pure(response))
      attempts <- calls.get
    } yield {
      assertEquals(sent, List(response))
      assertEquals(attempts, 1)
    }
  }

  test("a successful send with no file ID does not persist anything") {
    val markdown = MarkdownFile("f.md")
    for {
      calls <- Ref.of[IO, Int](0)
      repository = RepositoryMock(
        getResourceFileHandler = _ =>
          IO.pure(
            Right(
              NonEmptyList.one(
                MediaResource.MediaResourceFile(Repository.toTempFile[IO]("f.md", "body".getBytes()))
              )
            )
          ),
        registerFileIdHandler = (_, _, _) => calls.update(_ + 1)
      )
      response = TMessage(0, date = 0, chat = Chat(0, "private"), text = Some("rendered"))
      sent <- sendPattern(
        mediaFile = markdown,
        repository = repository,
        sendFileAPIMethod = (_, _, _) => IO.pure(response)
      )
      attempts <- calls.get
    } yield {
      assertEquals(sent, List(response))
      assertEquals(attempts, 0)
    }
  }

  private val storedReference =
    MediaResource.MediaResourceIFile[IO](MediaSource.FileRef(Platform.Telegram, "stored"))

  test("a successful send is counted once") {
    val mp3      = Mp3File("f.mp3")
    val response = messageWithFileId("stored", "audio")
    for {
      sends <- Ref.of[IO, List[MediaFile]](List.empty)
      repository = RepositoryMock(
        getResourceFileHandler = _ => IO.pure(Right(NonEmptyList.one(storedReference))),
        registerSendHandler = mf => sends.update(_ :+ mf)
      )
      sent <- sendPattern(mediaFile = mp3, repository = repository, sendFileAPIMethod = (_, _, _) => IO.pure(response))
      counted <- sends.get
    } yield {
      assertEquals(sent, List(response))
      assertEquals(counted, List(mp3))
    }
  }

  test("a failed send is not counted") {
    val mp3 = Mp3File("f.mp3")
    for {
      sends <- Ref.of[IO, Int](0)
      repository = RepositoryMock(
        getResourceFileHandler = _ => IO.pure(Right(NonEmptyList.one(storedReference))),
        registerSendHandler = _ => sends.update(_ + 1)
      )
      sent <- sendPattern(
        mediaFile = mp3,
        repository = repository,
        sendFileAPIMethod = (_, _, _) => IO.raiseError(new RuntimeException("telegram down"))
      )
      counted <- sends.get
    } yield {
      assertEquals(sent, List.empty)
      assertEquals(counted, 0)
    }
  }

  test("a failing count does not change the sent messages") {
    val mp3        = Mp3File("f.mp3")
    val response   = messageWithFileId("stored", "audio")
    val repository = RepositoryMock(
      getResourceFileHandler = _ => IO.pure(Right(NonEmptyList.one(storedReference))),
      registerSendHandler = _ => IO.raiseError(new RuntimeException("db down"))
    )
    sendPattern(mediaFile = mp3, repository = repository, sendFileAPIMethod = (_, _, _) => IO.pure(response))
      .map(sent => assertEquals(sent, List(response)))
  }
}
