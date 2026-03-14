package com.benkio.chattelegramadapter.http.telegramreply

import cats.data.NonEmptyList
import cats.effect.IO
import com.benkio.chatcore.messagefiltering.getContent
import com.benkio.chatcore.mocks.BackgroundJobManagerMock
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.MediaReply
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandTrigger
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.StringTextTriggerValue
import com.benkio.chatcore.model.TextTrigger
import com.benkio.chatcore.repository.db.DBMediaData
import com.benkio.chatcore.Logger.given
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.EffectfulKeyReply
import com.benkio.chattelegramadapter.mocks.ApiMock.given
import com.benkio.chattelegramadapter.mocks.SampleWebhookBot
import munit.*

class EffectfulKeyReplySpec extends CatsEffectSuite {

  val sBotInfo         = SampleWebhookBot.sBotInfo
  val message: Message = Message(
    messageId = 0,
    date = 0L,
    chatId = ChatId(0L),
    chatType = "private",
    text = Some("test message"),
    caption = None
  )
  val dbLayer              = DBLayerMock.mock(sBotInfo.botId)
  val repositoryMock       = RepositoryMock()
  val backgroundJobManager = BackgroundJobManagerMock.mock()

  test("EffectfulKeyReply.sendEffectfulKey should work for Random") {
    val dbLayerWithMedia = DBLayerMock.mock(
      sBotInfo.botId,
      medias = List(
        DBMediaData(
          media_name = "sbot_test.mp3",
          bot_id = sBotInfo.botId.value,
          kinds = """["audio"]""",
          mime_type = "audio/mpeg",
          media_sources = """["http://test.com"]""",
          media_count = 0,
          created_at = "1662126018293"
        )
      )
    )
    val repositoryMockWithHandler = RepositoryMock(
      getResourceFileHandler = _ => IO.pure(Right(NonEmptyList.one(MediaResource.MediaResourceIFile("test value"))))
    )
    val effectfulKey = EffectfulKey.Random(sBotInfo)

    val result = EffectfulKeyReply
      .sendEffectfulKey[IO](
        reply = effectfulKey,
        msg = message,
        ttl = None,
        repository = repositoryMockWithHandler,
        dbLayer = dbLayerWithMedia,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = Map.empty,
        replyToMessage = false
      )
      .map(messages => messages.map(_.text))

    assertIO(result, List(Some("[apiMock] sendMp3 reply")))
  }

  test("EffectfulKeyReply.sendEffectfulKey should work for SearchShow") {
    val effectfulKey = EffectfulKey.SearchShow(sBotInfo)

    val result = EffectfulKeyReply
      .sendEffectfulKey[IO](
        reply = effectfulKey,
        ttl = None,
        msg = message.copy(text = Some("/searchshow")),
        repository = repositoryMock,
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = Map.empty,
        replyToMessage = false
      )
      .map(messages => messages.map(_.text))

    assertIO(result, List(Some("[apiMock] sendMessage reply")))
  }

  test("EffectfulKeyReply.sendEffectfulKey should work for TriggerSearch") {
    val replyBundleMessages = List(
      ReplyBundleMessage(
        trigger = TextTrigger(StringTextTriggerValue("test")),
        reply = MediaReply(List(Mp3File("test.mp3"))),
        matcher = com.benkio.chatcore.messagefiltering.MessageMatches.ContainsOnce
      )
    )
    val effectfulKey = EffectfulKey.TriggerSearch(
      sBotInfo = sBotInfo,
      replyBundleMessage = replyBundleMessages,
      ignoreMessagePrefix = Some("!")
    )

    val result = EffectfulKeyReply
      .sendEffectfulKey[IO](
        reply = effectfulKey,
        ttl = None,
        msg = message.copy(text = Some("/triggersearch test")),
        repository = repositoryMock,
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = Map.empty,
        replyToMessage = false
      )
      .map(messages => messages.map(_.text))

    assertIO(result, List(Some("[apiMock] sendMessage reply")))
  }

  test("EffectfulKeyReply.sendEffectfulKey should work for Instructions") {
    val commands = List(
      ReplyBundleCommand(
        trigger = CommandTrigger("testcommand"),
        reply = TextReply.fromList("test reply")(false),
        instruction = CommandInstructionData.NoInstructions
      )
    )
    val effectfulKey = EffectfulKey.Instructions(
      sBotInfo = sBotInfo,
      ignoreMessagePrefix = Some("!"),
      commands = commands
    )

    val result = EffectfulKeyReply
      .sendEffectfulKey[IO](
        reply = effectfulKey,
        ttl = None,
        msg = message.copy(text = Some("/instructions")),
        repository = repositoryMock,
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = Map.empty,
        replyToMessage = false
      )
      .map(messages => messages.map(_.text))

    assertIO(result, List(Some("[apiMock] sendMessage reply")))
  }

  test("EffectfulKeyReply.sendEffectfulKey should work for Subscribe") {
    val effectfulKey = EffectfulKey.Subscribe(sBotInfo)

    val result = EffectfulKeyReply
      .sendEffectfulKey[IO](
        reply = effectfulKey,
        ttl = None,
        msg = message.copy(text = Some("/subscribe 0 * * ? * *")),
        repository = repositoryMock,
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = Map.empty,
        replyToMessage = false
      )
      .map(messages => messages.map(_.text))

    assertIO(result, List(Some("[apiMock] sendMessage reply")))
  }

  test("EffectfulKeyReply.sendEffectfulKey should work for Unsubscribe") {
    val effectfulKey = EffectfulKey.Unsubscribe(sBotInfo)

    val result = EffectfulKeyReply
      .sendEffectfulKey[IO](
        reply = effectfulKey,
        ttl = None,
        msg = message.copy(text = Some("/unsubscribe")),
        repository = repositoryMock,
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = Map.empty,
        replyToMessage = false
      )
      .map(messages => messages.map(_.text))

    assertIO(result, List(Some("[apiMock] sendMessage reply")))
  }

  test("EffectfulKeyReply.sendEffectfulKey should work for Subscriptions") {
    val effectfulKey = EffectfulKey.Subscriptions(sBotInfo)

    val result = EffectfulKeyReply
      .sendEffectfulKey[IO](
        reply = effectfulKey,
        msg = message.copy(text = Some("/subscriptions")),
        repository = repositoryMock,
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = Map.empty,
        replyToMessage = false,
        ttl = None
      )
      .map(messages => messages.map(_.text))

    assertIO(result, List(Some("[apiMock] sendMessage reply")))
  }

  test("EffectfulKeyReply.sendEffectfulKey should work for TopTwenty") {
    val effectfulKey     = EffectfulKey.TopTwenty(sBotInfo)
    val dbLayerWithMedia = DBLayerMock.mock(
      sBotInfo.botId,
      medias = List(
        DBMediaData(
          media_name = "sbot_test.mp3",
          bot_id = sBotInfo.botId.value,
          kinds = """["audio"]""",
          mime_type = "audio/mpeg",
          media_sources = """["http://test.com"]""",
          media_count = 0,
          created_at = "1662126018293"
        )
      )
    )
    val repositoryMockWithHandler = RepositoryMock(
      getResourceFileHandler = _ => IO.pure(Right(NonEmptyList.one(MediaResource.MediaResourceIFile("test value"))))
    )

    val result = EffectfulKeyReply
      .sendEffectfulKey[IO](
        reply = effectfulKey,
        msg = message,
        repository = repositoryMockWithHandler,
        dbLayer = dbLayerWithMedia,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = Map.empty,
        replyToMessage = false,
        ttl = None
      )
      .map(messages => messages.map(_.text))

    assertIO(result, List(Some("[apiMock] sendMessage reply")))
  }

  test("EffectfulKeyReply.sendEffectfulKey should work for Timeout") {
    val effectfulKey = EffectfulKey.Timeout(sBotInfo)

    val result = EffectfulKeyReply
      .sendEffectfulKey[IO](
        reply = effectfulKey,
        msg = message.copy(text = Some("/timeout")),
        repository = repositoryMock,
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = Map.empty,
        replyToMessage = false,
        ttl = None
      )
      .map(messages => messages.map(_.text))

    assertIO(result, List(Some("[apiMock] sendMessage reply")))
  }

  test("EffectfulKeyReply.sendEffectfulKey should work for MediaByKind") {
    val dbLayerWithMedia = DBLayerMock.mock(
      sBotInfo.botId,
      medias = List(
        DBMediaData(
          media_name = "sbot_test.mp3",
          bot_id = sBotInfo.botId.value,
          kinds = """["audio"]""",
          mime_type = "audio/mpeg",
          media_sources = """["http://test.com"]""",
          media_count = 0,
          created_at = "1662126018293"
        ),
        DBMediaData(
          media_name = "sbot_test_2.mp3",
          bot_id = sBotInfo.botId.value,
          kinds = """["audio"]""",
          mime_type = "audio/mpeg",
          media_sources = """["http://test2.com"]""",
          media_count = 0,
          created_at = "1662126018293"
        )
      )
    )
    val repositoryMockWithHandler = RepositoryMock(
      getResourceFileHandler = _ => IO.pure(Right(NonEmptyList.one(MediaResource.MediaResourceIFile("test value"))))
    )
    val effectfulKey = EffectfulKey.MediaByKind("audio", sBotInfo)

    val result = EffectfulKeyReply
      .sendEffectfulKey[IO](
        reply = effectfulKey,
        msg = message,
        repository = repositoryMockWithHandler,
        dbLayer = dbLayerWithMedia,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = Map.empty,
        replyToMessage = false,
        ttl = None
      )
      .map(messages => messages.map(_.text))

    assertIO(result, List(Some("[apiMock] sendMp3 reply")))
  }

  test("EffectfulKeyReply.sendEffectfulKey should work for Callback") {
    val callbackKey = "uppercase"
    // Setup a simple callback that takes the message text and makes it uppercase
    val callback: Message => IO[Text] =
      (msg: Message) => IO.pure(Text(msg.getContent.map(_.toUpperCase).getOrElse("")))
    val effectfulCallbacks = Map(callbackKey -> callback)
    val effectfulKey       = EffectfulKey.Callback(key = callbackKey, sBotInfo = sBotInfo)
    val testMessage        = message.copy(text = Some("hello world"))

    val result = EffectfulKeyReply
      .sendEffectfulKey[IO](
        reply = effectfulKey,
        msg = testMessage,
        repository = repositoryMock,
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = effectfulCallbacks,
        replyToMessage = false,
        ttl = None
      )
      .map(messages => messages.map(_.text))

    assertIO(result, List(Some("[apiMock] sendMessage reply")))
  }
}
