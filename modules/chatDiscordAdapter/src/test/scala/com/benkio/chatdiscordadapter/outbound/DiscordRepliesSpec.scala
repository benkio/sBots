package com.benkio.chatdiscordadapter.outbound

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.Ref
import cats.effect.Resource
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.NotifierMock
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.media.MediaResource.MediaResourceFile
import com.benkio.chatcore.model.reply.*
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageId
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBMediaData
import com.benkio.chatcore.repository.Repository.RepositoryError
import com.benkio.chatcore.Logger.given
import com.benkio.chatdiscordadapter.model.DiscordCapabilities
import com.benkio.chatdiscordadapter.model.DiscordLimits
import munit.CatsEffectSuite

import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path
import scala.concurrent.duration.*

class DiscordRepliesSpec extends CatsEffectSuite {

  private val botId = SBotId("bot")

  private val message = Message(
    messageId = Some(MessageId("111")),
    date = 0L,
    chatId = ChatId(Platform.Discord, "999"),
    chatType = "group",
    text = Some("hi")
  )

  /** What is asked to Discord, in order. */
  private class RecordingApi(calls: Ref[IO, List[String]]) extends DiscordApi[IO] {
    override def sendText(channelId: String, text: String, replyToMessageId: Option[String]): IO[String] =
      calls
        .update(_ :+ s"text:$channelId:${text.take(12)}:${text.length}:${replyToMessageId.getOrElse("-")}")
        .as("sent1")
    override def sendFile(channelId: String, fileName: String, file: Path, replyToMessageId: Option[String]) =
      calls.update(_ :+ s"file:$channelId:$fileName:${replyToMessageId.getOrElse("-")}").as("sent2")
    override def deleteMessage(channelId: String, messageId: String): IO[Unit] =
      calls.update(_ :+ s"delete:$channelId:$messageId")
  }

  private def tempFile(content: String): Path = {
    val path = Files.createTempFile("discordReplies", ".tmp")
    Files.writeString(path, content)
    path.toFile.deleteOnExit()
    path
  }

  private def resource(path: Path): MediaResource[IO] = MediaResourceFile(Resource.pure(path))

  private def mediaData(name: String): DBMediaData = DBMediaData(
    media_name = name,
    bot_id = "bot",
    kinds = "[]",
    mime_type = "audio/mpeg",
    media_sources = s"""["https://example.com/$name"]""",
    media_count = 0,
    created_at = "1"
  )

  private def run(
      value: ReplyValue,
      replyToMessage: Boolean = true,
      repository: RepositoryMock = new RepositoryMock(),
      medias: List[DBMediaData] = List.empty,
      msg: Message = message
  ): IO[(List[String], List[NotifierMock.MediaError])] =
    for {
      calls    <- Ref.of[IO, List[String]](List.empty)
      notifier <- NotifierMock.recording
      replies = {
        given com.benkio.chatcore.Notifier[IO] = notifier
        DiscordReplies[IO](
          new RecordingApi(calls),
          repository,
          DBLayerMock.mock(botId, medias = medias),
          DiscordCapabilities.value
        )
      }
      _      <- replies.send(value, msg, replyToMessage)
      done   <- calls.get
      errors <- notifier.mediaErrors.get
    } yield (done, errors)

  test("a text is sent to the channel, as a reply to the message") {
    run(value = Text("hello")).map((calls, _) => assertEquals(calls, List("text:999:hello:5:111")))
  }

  test("a text is not a reply when it is not asked, or when there is no message to reply to") {
    run(value = Text("hello"), replyToMessage = false).map((calls, _) =>
      assertEquals(calls, List("text:999:hello:5:-"))
    ) >>
      run(value = Text("hello"), msg = message.copy(messageId = None)).map((calls, _) =>
        assertEquals(calls, List("text:999:hello:5:-"))
      )
  }

  test("a markdown is sent as it is, a blank text is not sent") {
    run(value = Markdown("**hi**")).map((calls, _) => assertEquals(calls, List("text:999:**hi**:6:111"))) >>
      run(value = Text("  ")).map((calls, _) => assertEquals(calls, List.empty))
  }

  test("a text with a time to live is deleted after it") {
    run(value = Text("bye", Some(50.millis)))
      .flatMap((first, _) =>
        // the delete runs in a fiber: the recorded calls are read again after the time to live
        IO.sleep(200.millis).as(first)
      )
      .map(first => assertEquals(first, List("text:999:bye:3:111")))
  }

  test("a media is uploaded with its name, as a reply") {
    val path = tempFile("data")
    run(
      value = Mp3File("bot_song.mp3"),
      repository = new RepositoryMock(getResourceFileHandler = _ => IO.pure(Right(NonEmptyList.one(resource(path)))))
    ).map((calls, _) => assertEquals(calls, List("file:999:bot_song.mp3:111")))
  }

  test("the next resource is used when the previous one fails") {
    val path   = tempFile("data")
    val broken = MediaResourceFile[IO](Resource.eval(IO.raiseError[Path](new RuntimeException("no"))))
    run(
      value = Mp3File("bot_song.mp3"),
      repository =
        new RepositoryMock(getResourceFileHandler = _ => IO.pure(Right(NonEmptyList.of(broken, resource(path)))))
    ).map((calls, errors) => {
      assertEquals(calls, List("file:999:bot_song.mp3:111"))
      assertEquals(errors, List.empty)
    })
  }

  test("a file above the upload limit is sent as its link") {
    val path = Files.createTempFile("discordBig", ".tmp")
    path.toFile.deleteOnExit()
    val file = new RandomAccessFile(path.toFile, "rw")
    file.setLength(DiscordLimits.maxUploadBytes + 1)
    file.close()
    run(
      value = Mp3File("bot_song.mp3"),
      repository = new RepositoryMock(getResourceFileHandler = _ => IO.pure(Right(NonEmptyList.one(resource(path))))),
      medias = List(mediaData("bot_song.mp3"))
    ).map((calls, _) => assertEquals(calls, List("text:999:https://exam:32:111")))
  }

  test("a media that cannot be read is notified and nothing is sent") {
    run(
      value = Mp3File("bot_song.mp3"),
      repository = new RepositoryMock(getResourceFileHandler =
        mediaFile => IO.pure(Left(RepositoryError.NoResourcesFoundFile(mediaFile)))
      )
    ).map((calls, errors) => {
      assertEquals(calls, List.empty)
      assertEquals(errors.map(_.mediaFile), List(Mp3File("bot_song.mp3")))
    })
  }

  test("a markdown file is sent as text, split to the limit, replying only with the first part") {
    val path = tempFile(("word " * 500).trim)
    run(
      value = MarkdownFile("bot_doc.md"),
      repository = new RepositoryMock(getResourceFileHandler = _ => IO.pure(Right(NonEmptyList.one(resource(path)))))
    ).map((calls, _) => {
      assertEquals(calls.size, 2)
      assert(calls.head.endsWith(":111"), calls.head)
      assert(calls(1).endsWith(":-"), calls(1))
    })
  }
}
