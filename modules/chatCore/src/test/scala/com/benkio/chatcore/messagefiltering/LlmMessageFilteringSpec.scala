package com.benkio.chatcore.messagefiltering

import cats.effect.kernel.Ref
import cats.effect.IO
import cats.syntax.all.*
import com.benkio.chatcore.llm.LlmSettings
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.FakePlatformAdapter
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.db.DBTimeoutData
import com.benkio.chatcore.PlatformAdapter
import com.benkio.chatcore.TestIds
import com.benkio.chatcore.UsageStats.Outcome
import munit.CatsEffectSuite

import java.time.Instant
import scala.concurrent.duration.*

class LlmMessageFilteringSpec extends CatsEffectSuite {

  private val botInfo = SBotInfo(botId = SBotInfo.SBotId("eng"), botName = SBotInfo.SBotName("EngineBot"))

  private def message(
      text: Option[String],
      caption: Option[String] = None,
      chatType: String = "group",
      isForward: Boolean = false
  ): Message =
    Message(
      messageId = None,
      date = 0L,
      chatId = TestIds.chatId(1L),
      chatType = chatType,
      text = text,
      caption = caption,
      isForward = isForward
    )

  /** The pure conditions: the guard on a chat with the LLM on and no timeout, so only the filter decides. */
  private def filter(msg: Message, outcome: Outcome = Outcome.UnReplied): IO[Boolean] =
    guard(msg = msg, outcome = outcome).map(_._1)

  test("an unanswered plain message that mentions the bot passes") {
    filter(message(text = Some("hey EngineBot, how are you?"))).assertEquals(true)
  }

  test("the caption of a message is searched when it has no text") {
    filter(message(text = None, caption = Some("look EngineBot"))).assertEquals(true)
  }

  test("only the unanswered outcome passes") {
    Outcome.values.toList.traverse_(outcome =>
      filter(message(text = Some("hey EngineBot")), outcome).map(passes =>
        assertEquals(passes, outcome == Outcome.UnReplied, s"outcome ${outcome.label}")
      )
    )
  }

  test("a message that does not mention the bot is rejected") {
    filter(message(text = Some("hello everybody"))).assertEquals(false)
  }

  test("the mention is case insensitive") {
    filter(message(text = Some("hey enginebot"))).assertEquals(true) *>
      filter(message(text = Some("HEY ENGINEBOT"))).assertEquals(true)
  }

  test("a message without any content is rejected") {
    filter(message(text = None)).assertEquals(false)
  }

  test("the bot name is matched as a whole word") {
    filter(message(text = Some("EngineBotany is nice"))).assertEquals(false) *>
      filter(message(text = Some("hey, EngineBot!"))).assertEquals(true)
  }

  test("a private chat needs no mention, but still some text") {
    filter(message(text = Some("hello"), chatType = "private")).assertEquals(true) *>
      filter(message(text = None, chatType = "private")).assertEquals(false)
  }

  test("a forwarded message is rejected even when it mentions the bot") {
    filter(message(text = Some("hey EngineBot"), isForward = true)).assertEquals(false)
  }

  test("a command that mentions the bot is rejected") {
    filter(message(text = Some("/hi EngineBot"))).assertEquals(false)
  }

  // guard: the chat state ///////////////////////////////////////////////////////////////////////////////////////

  private def llmSettings(enabled: Boolean, reads: Ref[IO, Int]): LlmSettings[IO] =
    new LlmSettings[IO] {
      override def enable(chat: ChatId): IO[Unit]               = IO.unit
      override def disable(chat: ChatId): IO[Unit]              = IO.unit
      override def forget(chat: ChatId): IO[Unit]               = IO.unit
      override def status(chat: ChatId): IO[LlmSettings.Status] =
        reads.update(_ + 1).as(LlmSettings.Status(enabled = enabled))
    }

  private def inTimeout: List[DBTimeoutData] = List(
    DBTimeoutData(
      platform = "telegram",
      chat_id = "1",
      bot_id = botInfo.botId.value,
      timeout_value = 30.seconds.toMillis.toString,
      last_interaction = Instant.now.getEpochSecond.toString
    )
  )

  private def runGuard(dbLayer: DBLayer[IO], msg: Message, outcome: Outcome)(using
      PlatformAdapter[IO]
  ): IO[Boolean] =
    LlmMessageFiltering.guard[IO](dbLayer, botInfo)(msg, outcome)

  /** The guard of the message, and how many times the `/llm` switch of the chat was read. */
  private def guard(
      msg: Message,
      enabled: Boolean = true,
      timeouts: List[DBTimeoutData] = List.empty,
      outcome: Outcome = Outcome.UnReplied
  ): IO[(Boolean, Int)] = for {
    reads   <- Ref.of[IO, Int](0)
    adapter <- FakePlatformAdapter(capabilities =
      FakePlatformAdapter.textOnlyCapabilities.copy(commandSyntax = CommandSyntax.Slash)
    )
    dbLayer = DBLayerMock.mock(botInfo.botId, timeouts = timeouts, llmSettings = llmSettings(enabled, reads))
    passes <- runGuard(dbLayer = dbLayer, msg = msg, outcome = outcome)(using adapter)
    count  <- reads.get
  } yield (passes, count)

  private val addressed = message(text = Some("hey EngineBot"))

  test("the guard passes an addressed message of a chat with the LLM on and no timeout") {
    guard(msg = addressed).assertEquals((true, 1))
  }

  test("the guard rejects a chat with the LLM off") {
    guard(msg = addressed, enabled = false).assertEquals((false, 1))
  }

  test("the guard rejects a chat that is in timeout, without reading the switch") {
    guard(msg = addressed, timeouts = inTimeout).assertEquals((false, 0))
  }

  test("the guard does not read the chat state for a message the pure conditions reject") {
    guard(msg = message(text = Some("hello everybody"))).assertEquals((false, 0)) *>
      guard(msg = addressed, outcome = Outcome.ReactiveReplied).assertEquals((false, 0))
  }
}
