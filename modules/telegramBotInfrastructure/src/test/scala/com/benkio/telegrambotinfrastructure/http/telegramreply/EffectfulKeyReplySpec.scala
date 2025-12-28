package com.benkio.telegrambotinfrastructure.http.telegramreply

import cats.data.NonEmptyList
import cats.effect.IO
import com.benkio.telegrambotinfrastructure.http.telegramreply.EffectfulKeyReply
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.mocks.BackgroundJobManagerMock
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.mocks.RepositoryMock
import com.benkio.telegrambotinfrastructure.mocks.SampleWebhookBot
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.model.reply.EffectfulKey
import com.benkio.telegrambotinfrastructure.model.reply.MediaReply
import com.benkio.telegrambotinfrastructure.model.reply.Mp3File
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.reply.TextReply
import com.benkio.telegrambotinfrastructure.model.CommandInstructionData
import com.benkio.telegrambotinfrastructure.model.CommandTrigger
import com.benkio.telegrambotinfrastructure.model.StringTextTriggerValue
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import com.benkio.telegrambotinfrastructure.repository.db.DBMediaData
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.*
import telegramium.bots.Chat
import telegramium.bots.Message

class EffectfulKeyReplySpec extends CatsEffectSuite {
  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  val sBotInfo             = SampleWebhookBot.sBotInfo
  val message              = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some("test message"))
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
      getResourceFileHandler = _ => IO.pure(NonEmptyList.one(MediaResource.MediaResourceIFile("test value")))
    )
    val effectfulKey = EffectfulKey.Random(sBotInfo)

    val result = EffectfulKeyReply
      .sendEffectfulKey[IO](
        reply = effectfulKey,
        msg = message,
        repository = repositoryMockWithHandler,
        dbLayer = dbLayerWithMedia,
        backgroundJobManager = backgroundJobManager,
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
        msg = message.copy(text = Some("/searchshow")),
        repository = repositoryMock,
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
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
        matcher = com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches.ContainsOnce
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
        msg = message.copy(text = Some("/triggersearch test")),
        repository = repositoryMock,
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
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
        msg = message.copy(text = Some("/instructions")),
        repository = repositoryMock,
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
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
        msg = message.copy(text = Some("/subscribe 0 * * ? * *")),
        repository = repositoryMock,
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
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
        msg = message.copy(text = Some("/unsubscribe")),
        repository = repositoryMock,
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
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
        replyToMessage = false
      )
      .map(messages => messages.map(_.text))

    assertIO(result, List(Some("[apiMock] sendMessage reply")))
  }

  test("EffectfulKeyReply.sendEffectfulKey should work for TopTwenty") {
    val effectfulKey = EffectfulKey.TopTwenty(sBotInfo)

    val result = EffectfulKeyReply
      .sendEffectfulKey[IO](
        reply = effectfulKey,
        msg = message,
        repository = repositoryMock,
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
        replyToMessage = false
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
        replyToMessage = false
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
      getResourceFileHandler = _ => IO.pure(NonEmptyList.one(MediaResource.MediaResourceIFile("test value")))
    )
    val effectfulKey = EffectfulKey.MediaByKind("audio", sBotInfo)

    val result = EffectfulKeyReply
      .sendEffectfulKey[IO](
        reply = effectfulKey,
        msg = message,
        repository = repositoryMockWithHandler,
        dbLayer = dbLayerWithMedia,
        backgroundJobManager = backgroundJobManager,
        replyToMessage = false
      )
      .map(messages => messages.map(_.text))

    assertIO(result, List(Some("[apiMock] sendMp3 reply")))
  }

  test("EffectfulKeyReply.sendEffectfulKey should work for Callback") {
    ???
  }
}
