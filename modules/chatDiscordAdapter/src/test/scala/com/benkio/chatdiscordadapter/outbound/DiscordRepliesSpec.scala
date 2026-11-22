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
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageId
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.SubscriptionDraftStep
import com.benkio.chatcore.model.SubscriptionFlowAction
import com.benkio.chatcore.model.SubscriptionFlowButton
import com.benkio.chatcore.model.SubscriptionFlowKey
import com.benkio.chatcore.model.SubscriptionFlowPrompt
import com.benkio.chatcore.repository.db.DBMediaData
import com.benkio.chatcore.repository.Repository.RepositoryError
import com.benkio.chatcore.Logger.given
import com.benkio.chatcore.SubscriptionFlowPromptStore
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
    override def sendButtons(channelId: String, message: DiscordButtonMessage, replyToMessageId: Option[String]) =
      calls
        .update(
          _ :+ s"buttons:$channelId:${message.rows.map(_.map(_.label).mkString(",")).mkString("|")}:${replyToMessageId.getOrElse("-")}"
        )
        .as("sent3")
    override def editButtons(channelId: String, messageId: String, message: DiscordButtonMessage) =
      calls.update(_ :+ s"edit:$messageId:${message.rows.map(_.map(_.label).mkString(",")).mkString("|")}")
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
      lists    <- DiscordButtonLists.create[IO]()
      flowPrompts = SubscriptionFlowPromptStore.create[IO](1.minute, 10)
      replies     = {
        given com.benkio.chatcore.Notifier[IO] = notifier
        DiscordReplies[IO](
          new RecordingApi(calls),
          repository,
          DBLayerMock.mock(botId, medias = medias),
          DiscordCapabilities.value,
          lists,
          flowPrompts
        )
      }
      _      <- replies.send(value, msg, replyToMessage)
      done   <- calls.get
      errors <- notifier.mediaErrors.get
    } yield (done, errors)

  test("a paged list is sent with its buttons, as a reply to the message") {
    val pagedList = PagedList(
      title = "Top",
      items = List(PagedItem.ValueItem(Mp3File("a.mp3")), PagedItem.ValueItem(Mp3File("b.mp3"))),
      page = 0,
      commandKey = CommandKey.TopTwenty
    )
    run(value = pagedList).map((calls, _) => assertEquals(calls, List("buttons:999:1,2:111")))
  }

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

  test("an uploaded media is counted once, a media sent as its link is not") {
    val small = tempFile("data")
    val big   = Files.createTempFile("discordBig", ".tmp")
    big.toFile.deleteOnExit()
    val file = new RandomAccessFile(big.toFile, "rw")
    file.setLength(DiscordLimits.maxUploadBytes + 1)
    file.close()
    def counted(path: Path): IO[Int] =
      for {
        sends <- Ref.of[IO, Int](0)
        _     <- run(
          value = Mp3File("bot_song.mp3"),
          repository = new RepositoryMock(
            getResourceFileHandler = _ => IO.pure(Right(NonEmptyList.one(resource(path)))),
            registerSendHandler = _ => sends.update(_ + 1)
          ),
          medias = List(mediaData("bot_song.mp3"))
        )
        count <- sends.get
      } yield count
    for {
      uploaded <- counted(small)
      link     <- counted(big)
    } yield {
      assertEquals(uploaded, 1)
      assertEquals(link, 0)
    }
  }

  test("a media that fails to be read is not counted") {
    for {
      sends <- Ref.of[IO, Int](0)
      _     <- run(
        value = Mp3File("bot_song.mp3"),
        repository = new RepositoryMock(
          getResourceFileHandler = mediaFile => IO.pure(Left(RepositoryError.NoResourcesFoundFile(mediaFile))),
          registerSendHandler = _ => sends.update(_ + 1)
        )
      )
      count <- sends.get
    } yield assertEquals(count, 0)
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

  private def promptFixture: IO[(Ref[IO, List[String]], SubscriptionFlowPromptStore[IO], DiscordReplies[IO])] =
    for {
      calls    <- Ref.of[IO, List[String]](List.empty)
      lists    <- DiscordButtonLists.create[IO]()
      notifier <- NotifierMock.recording
      store   = SubscriptionFlowPromptStore.create[IO](1.minute, 10)
      replies = {
        given com.benkio.chatcore.Notifier[IO] = notifier
        DiscordReplies[IO](
          new RecordingApi(calls),
          new RepositoryMock(),
          DBLayerMock.mock(botId),
          DiscordCapabilities.value,
          lists,
          store
        )
      }
    } yield (calls, store, replies)

  /** Fails the first edit (after recording it) with a given error, then edits normally. */
  private class EditFailingApi(calls: Ref[IO, List[String]], error: Throwable, failed: Ref[IO, Boolean])
      extends RecordingApi(calls) {
    override def editButtons(channelId: String, messageId: String, message: DiscordButtonMessage): IO[Unit] =
      super.editButtons(channelId = channelId, messageId = messageId, message = message) >>
        failed.get.flatMap(happened => if happened then IO.unit else failed.set(true) >> IO.raiseError(error))
  }

  private def promptReplies(store: SubscriptionFlowPromptStore[IO], api: DiscordApi[IO]): IO[DiscordReplies[IO]] =
    for {
      lists    <- DiscordButtonLists.create[IO]()
      notifier <- NotifierMock.recording
    } yield {
      given com.benkio.chatcore.Notifier[IO] = notifier
      DiscordReplies[IO](api, new RepositoryMock(), DBLayerMock.mock(botId), DiscordCapabilities.value, lists, store)
    }

  private def flowPrompt(step: SubscriptionDraftStep, label: String): SubscriptionFlowPrompt =
    SubscriptionFlowPrompt(
      key = SubscriptionFlowKey(message.chatId, botId, "alice"),
      step = step,
      text = s"step $step",
      buttons = List(List(SubscriptionFlowButton(label, SubscriptionFlowAction.confirm)))
    )

  test("a subscription prompt is sent with buttons and its next step edits the same message") {
    for {
      (calls, store, replies) <- promptFixture
      _                       <- replies.send(flowPrompt(SubscriptionDraftStep.Type, "show"), message, false)
      _                       <- replies.send(flowPrompt(SubscriptionDraftStep.Confirm, "confirm"), message, false)
      done                    <- calls.get
      handle                  <- store.byKey(SubscriptionFlowKey(message.chatId, botId, "alice"))
    } yield {
      assertEquals(done, List("buttons:999:show:-", "edit:sent3:confirm"))
      assertEquals(handle.map(_.messageId), Some("sent3"))
      assertEquals(handle.map(_.owner), Some("alice"))
    }
  }

  test("a deleted prompt is replaced with a new message and the next step edits that one") {
    val key = SubscriptionFlowKey(message.chatId, botId, "alice")
    for {
      calls  <- Ref.of[IO, List[String]](List.empty)
      failed <- Ref.of[IO, Boolean](false)
      store = SubscriptionFlowPromptStore.create[IO](1.minute, 10)
      _       <- store.put(key, "old", "alice")
      replies <- promptReplies(store, new EditFailingApi(calls, new DiscordUnknownMessage("old"), failed))
      _       <- replies.send(flowPrompt(SubscriptionDraftStep.Confirm, "confirm"), message, false)
      after   <- calls.get
      handle  <- store.byKey(key)
      oldRef  <- store.byMessageId(message.chatId, "old")
      newRef  <- store.byMessageId(message.chatId, "sent3")
      _       <- replies.send(flowPrompt(SubscriptionDraftStep.Type, "next"), message, false)
      done    <- calls.get
    } yield {
      assertEquals(after, List("edit:old:confirm", "buttons:999:confirm:-"))
      assertEquals(handle.map(_.messageId), Some("sent3"))
      assertEquals(oldRef, None)
      assertEquals(newRef.map(_.key), Some(key))
      assertEquals(done, List("edit:old:confirm", "buttons:999:confirm:-", "edit:sent3:next"))
    }
  }

  test("other prompt edit failures keep the handle and send no replacement") {
    val key = SubscriptionFlowKey(message.chatId, botId, "alice")
    for {
      calls  <- Ref.of[IO, List[String]](List.empty)
      failed <- Ref.of[IO, Boolean](false)
      store = SubscriptionFlowPromptStore.create[IO](1.minute, 10)
      _       <- store.put(key, "old", "alice")
      replies <- promptReplies(store, new EditFailingApi(calls, new RuntimeException("boom"), failed))
      _       <- replies.send(flowPrompt(SubscriptionDraftStep.Confirm, "confirm"), message, false)
      done    <- calls.get
      handle  <- store.byKey(key)
    } yield {
      assertEquals(done, List("edit:old:confirm"))
      assertEquals(handle.map(_.messageId), Some("old"))
    }
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
