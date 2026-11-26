package com.benkio.chatcore

import cats.effect.IO
import cats.effect.Ref
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.llm.LlmResponder
import com.benkio.chatcore.llm.LlmSettings
import com.benkio.chatcore.mocks.BackgroundJobManagerMock
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.FakePlatformAdapter
import com.benkio.chatcore.model.reply.Reply
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Mention
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.RepliedMessage
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Logger.given
import com.benkio.chatcore.TestIds
import munit.CatsEffectSuite
import org.http4s.syntax.literals.*

import java.time.Instant
import scala.concurrent.duration.*

import _root_.log.effect.internal.Show
import _root_.log.effect.LogLevel
import _root_.log.effect.LogWriter

/** The routing of a message: the reactive replies first, then the LLM for the messages directed at the bot. */
class BotEngineLlmSpec extends CatsEffectSuite {

  private val sBotInfo: SBotInfo = SBotInfo(botId = SBotInfo.SBotId("eng"), botName = SBotInfo.SBotName("EngineBot"))

  private val sBotConfig: SBotConfig = SBotConfig(
    sBotInfo = sBotInfo,
    triggerFilename = "eng_triggers.md",
    triggerListUri = uri"https://example.com/eng_triggers.md",
    instructionsFilename = "eng_instructions.md",
    instructionsUri = uri"https://example.com/eng_instructions.md",
    token = "eng.token",
    listJsonFilename = "eng_list.json",
    repliesJsonFilename = "eng_replies.json",
    commandsJsonFilename = "eng_commands.json",
    showFilename = "eng_shows.json"
  )

  private val messageReplies = List(ReplyBundleMessage.textToText("ping")("pong"))

  private def settings(enabled: Boolean): LlmSettings[IO] = new LlmSettings[IO] {
    def enable(chatId: ChatId): IO[Unit]               = IO.unit
    def disable(chatId: ChatId): IO[Unit]              = IO.unit
    def status(chatId: ChatId): IO[LlmSettings.Status] = IO.pure(LlmSettings.Status(enabled))
    def forget(chatId: ChatId): IO[Unit]               = IO.unit
  }

  private def message(text: String): Message =
    Message(None, Instant.now.getEpochSecond, TestIds.chatId(1L), "private", text = Some(text))

  private val llmAnswer: LlmResponder[IO] = _ =>
    IO.pure(Some(TextReply.fromList("llm says hi")(replyToMessage = true)): Option[Reply])

  private final case class Run(sent: List[ReplyValue], calls: Int, typings: Int, interactions: Int)

  private def recordingLog(lines: Ref[IO, List[String]]): LogWriter[IO] = new LogWriter[IO] {
    override def write[A](level: LogLevel, a: => A)(implicit show: Show[A]): IO[Unit] =
      lines.update(_ :+ show.show(a))
  }

  /** What the platform receives, how many times the responder was called, typing actions and post computations. */
  private def runAll(
      messages: List[Message],
      enabled: Boolean = true,
      responder: LlmResponder[IO] = llmAnswer,
      llmTimeout: FiniteDuration = 1.second,
      requestTimeout: Option[FiniteDuration] = None,
      filter: IO[Boolean] = IO.pure(true),
      stallInteraction: Boolean = false,
      config: SBotConfig => SBotConfig = identity,
      log: LogWriter[IO] = summon[LogWriter[IO]],
      typingFailure: Option[Throwable] = None
  ): IO[Run] = for {
    adapter      <- FakePlatformAdapter(typingFailure = typingFailure)
    calls        <- Ref.of[IO, Int](0)
    interactions <- Ref.of[IO, Int](0)
    counting: LlmResponder[IO] = new LlmResponder[IO] {
      override def answers: Boolean                                             = responder.answers
      override def remember(msg: Message)(using cats.Applicative[IO]): IO[Unit] = responder.remember(msg)
      override def rememberAnswer(msg: Message, reply: Reply)(using
          cats.Applicative[IO]
      ): IO[Unit]                                           = responder.rememberAnswer(msg, reply)
      override def respond(msg: Message): IO[Option[Reply]] =
        calls.update(_ + 1) *> responder.respond(msg)
    }
    engine = new BotEngine[IO](
      sBotConfig = config(sBotConfig.copy(requestProcessingTimeout = requestTimeout)),
      dbLayer = DBLayerMock.mock(sBotInfo.botId, messageReplies = messageReplies, llmSettings = settings(enabled)),
      filteringMatchesMessages = (_, _) => filter,
      postComputation = _ => if stallInteraction then IO.never else interactions.update(_ + 1),
      notifier = Notifier.noop[IO],
      backgroundJobManager = BackgroundJobManagerMock.mock(),
      llmResponder = counting,
      llmTimeout = llmTimeout
    )(using IO.asyncForIO, log)
    _    <- messages.parTraverse_(engine.onMessage(_)(using adapter))
    sent <- adapter.sent.get
    n    <- calls.get
    t    <- adapter.typings.get
    i    <- interactions.get
  } yield Run(sent.map(_.replyValue), n, t, i)

  private def run(
      text: String,
      enabled: Boolean = true,
      responder: LlmResponder[IO] = llmAnswer,
      log: LogWriter[IO] = summon[LogWriter[IO]]
  ): IO[Run] =
    runAll(messages = List(message(text)), enabled = enabled, responder = responder, log = log)

  private val noReply: LlmResponder[IO] = _ => IO.pure(None)

  /** The engine logs a skip reason instead of an interaction line, so capture the plain log to explain a non-reply. */
  private def runMessageWithLog(
      msg: Message,
      enabled: Boolean = true,
      responder: LlmResponder[IO] = llmAnswer,
      filter: IO[Boolean] = IO.pure(true),
      config: SBotConfig => SBotConfig = identity
  ): IO[List[String]] =
    for {
      lines <- Ref.of[IO, List[String]](List.empty)
      _     <- runAll(
        messages = List(msg),
        enabled = enabled,
        responder = responder,
        filter = filter,
        config = config,
        log = recordingLog(lines)
      )
      seen <- lines.get
    } yield seen.filter(_.contains("LLM not asked"))

  private def runWithLog(
      text: String,
      enabled: Boolean = true,
      responder: LlmResponder[IO] = llmAnswer,
      filter: IO[Boolean] = IO.pure(true),
      config: SBotConfig => SBotConfig = identity
  ): IO[List[String]] =
    runMessageWithLog(msg = message(text), enabled = enabled, responder = responder, filter = filter, config = config)

  test("a directed message with no reactive match gets the LLM reply, after a typing action") {
    run(text = "EngineBot: how are you?").map { case Run(sent, calls, typings, _) =>
      assertEquals(sent, List[ReplyValue](Text("llm says hi")))
      assertEquals(calls, 1)
      assertEquals(typings, 1)
    }
  }

  test("a reactive match wins: the LLM is not called") {
    run(text = "EngineBot: ping").map { case Run(sent, calls, _, _) =>
      assertEquals(sent, List[ReplyValue](Text("pong")))
      assertEquals(calls, 0)
    }
  }

  test("a message not directed at the bot never reaches the LLM") {
    run(text = "how are you?").map { case Run(sent, calls, _, _) =>
      assertEquals(sent, List.empty[ReplyValue])
      assertEquals(calls, 0)
    }
  }

  test("a chat with the LLM disabled never reaches the LLM") {
    run(text = "EngineBot: how are you?", enabled = false).map { case Run(sent, calls, _, _) =>
      assertEquals(sent, List.empty[ReplyValue])
      assertEquals(calls, 0)
    }
  }

  test("a command never reaches the LLM") {
    run(text = "!llm").map { r => assertEquals(r.calls, 0) }
  }

  test("a matching bundle blocked by the filters (the chat timeout) does not fall through to the LLM") {
    runAll(messages = List(message("EngineBot: ping")), filter = IO.pure(false)).map { case Run(sent, calls, _, _) =>
      assertEquals(sent, List.empty[ReplyValue])
      assertEquals(calls, 0)
    }
  }

  test("a slow LLM is dropped silently after its own timeout") {
    runAll(messages = List(message("EngineBot: how are you?")), responder = _ => IO.never, llmTimeout = 50.millis)
      .map { case Run(sent, calls, _, _) =>
        assertEquals(sent, List.empty[ReplyValue])
        assertEquals(calls, 1)
      }
  }

  test("a failing LLM is dropped and the message does not fail") {
    run(text = "EngineBot: how are you?", responder = _ => IO.raiseError(new RuntimeException("boom"))).map(r =>
      assertEquals(r.sent, List.empty[ReplyValue])
    )
  }

  test("an LLM reply counts as an interaction (the post computation runs); no reply does not") {
    for {
      answered <- run(text = "EngineBot: hello")
      silent   <- run(text = "EngineBot: hello", responder = noReply)
    } yield {
      assertEquals(answered.interactions, 1)
      assertEquals(silent.interactions, 0)
      assertEquals(silent.sent, List.empty[ReplyValue])
    }
  }

  test("the noop responder is skipped altogether: no typing action, no LLM call") {
    run(text = "EngineBot: hello", responder = LlmResponder.noop[IO]).map(r =>
      assertEquals((r.calls, r.typings), (0, 0))
    )
  }

  test("a mention or a reply to the bot is directed at it") {
    val mention = message("what do you think?").copy(mentions = List(Mention(name = Some("EngineBot"))))
    val reply   = message("what do you think?").copy(replyTo = Some(RepliedMessage(authorName = Some("EngineBot"))))
    List(mention, reply)
      .traverse(m => runAll(messages = List(m)).map(_.sent))
      .map(sent =>
        assertEquals(sent, List(List[ReplyValue](Text("llm says hi")), List[ReplyValue](Text("llm says hi"))))
      )
  }

  test("a forwarded, an old or an ignored-prefix message never reaches the LLM") {
    val forwarded = message("EngineBot: hi").copy(isForward = true)
    val old       = message("EngineBot: hi").copy(date = Instant.now.getEpochSecond - 1.day.toSeconds)
    val ignored   = message("!EngineBot: hi")
    runAll(messages = List(forwarded, old, ignored), config = _.copy(disableForward = true)).map(r =>
      assertEquals(r.calls, 0)
    )
  }

  test("each engine gate logs why the LLM was not asked") {
    val directed = "EngineBot: how are you?"
    val ignored  = message("zzz hello").copy(mentions = List(Mention(name = Some("EngineBot"))))
    for {
      notDirected <- runWithLog(text = "how are you?")
      disabled    <- runWithLog(text = directed, enabled = false)
      forwarded   <- runMessageWithLog(msg = message(directed).copy(isForward = true))
      old <- runMessageWithLog(msg = message(directed).copy(date = Instant.now.getEpochSecond - 1.day.toSeconds))
      ignoredLine  <- runMessageWithLog(msg = ignored, config = _.copy(ignoreMessagePrefix = Some("zzz")))
      responderOff <- runWithLog(text = directed, responder = LlmResponder.noop[IO])
      blocked      <- runWithLog(text = "EngineBot: ping", filter = IO.pure(false))
      silent       <- runWithLog(text = directed, responder = noReply)
    } yield {
      def only(lines: List[String], reason: String): Boolean =
        lines.size == 1 && lines.head.contains(s"reason=$reason")
      assert(only(notDirected, "not_directed"), notDirected.toString)
      assert(only(disabled, "disabled"), disabled.toString)
      assert(only(forwarded, "forwarded"), forwarded.toString)
      assert(only(old, "too_old"), old.toString)
      assert(only(ignoredLine, "ignored_prefix"), ignoredLine.toString)
      assert(only(responderOff, "responder_off"), responderOff.toString)
      assert(only(blocked, "reactive_blocked"), blocked.toString)
      assert(only(silent, "no_reply"), silent.toString)
    }
  }

  test("a second directed message of a chat logs the in-flight skip") {
    val slow: LlmResponder[IO] =
      _ => IO.sleep(200.millis).as(Some(TextReply.fromList("late")(replyToMessage = true)))
    for {
      lines <- Ref.of[IO, List[String]](List.empty)
      _     <- runAll(
        messages = List(message("EngineBot: one"), message("EngineBot: two")),
        responder = slow,
        log = recordingLog(lines)
      )
      seen <- lines.get
    } yield assertEquals(seen.filter(_.contains("reason=in_flight")).size, 1)
  }

  test("a duplicate message reaches the LLM once") {
    val m = message("EngineBot: hi").copy(messageId = Some(TestIds.messageId(5)))
    runAll(messages = List(m, m)).map(r => assertEquals(r.calls, 1))
  }

  test("a second directed message of a chat is not asked while the first is in flight") {
    val slow: LlmResponder[IO] =
      _ => IO.sleep(100.millis).as(Some(TextReply.fromList("late")(replyToMessage = true)))
    runAll(messages = List(message("EngineBot: one"), message("EngineBot: two")), responder = slow).map(r =>
      assertEquals((r.calls, r.sent.size), (1, 1))
    )
  }

  test("a slow reactive part is cut by the request timeout and the LLM is not run") {
    runAll(
      messages = List(message("EngineBot: ping")),
      requestTimeout = Some(50.millis),
      filter = IO.sleep(300.millis).as(true)
    ).map(r => assertEquals((r.calls, r.sent), (0, List.empty[ReplyValue])))
  }

  test("the LLM runs outside the request timeout: a slow answer still completes") {
    val slow: LlmResponder[IO] =
      _ => IO.sleep(150.millis).as(Some(TextReply.fromList("late")(replyToMessage = true)))
    runAll(messages = List(message("EngineBot: hello")), responder = slow, requestTimeout = Some(50.millis)).map(r =>
      assertEquals(r.sent, List[ReplyValue](Text("late")))
    )
  }

  /** The calls of the memory hooks and of respond, in order. */
  private def recording(events: Ref[IO, List[String]], answer: Option[Reply]): LlmResponder[IO] =
    new LlmResponder[IO] {
      override def respond(msg: Message): IO[Option[Reply]] =
        events.update(_ :+ "respond").as(answer)
      override def remember(msg: Message)(using cats.Applicative[IO]): IO[Unit] = events.update(_ :+ "remember")
      override def rememberAnswer(msg: Message, reply: Reply)(using
          cats.Applicative[IO]
      ): IO[Unit] = events.update(_ :+ "rememberAnswer")
    }

  private def eventsOf(text: String, answer: Option[Reply]): IO[List[String]] = for {
    events <- Ref.of[IO, List[String]](List.empty)
    _      <- runAll(messages = List(message(text)), responder = recording(events, answer))
    seen   <- events.get
  } yield seen

  test("the message is remembered after the LLM answered it, then the answer") {
    eventsOf("EngineBot: hello", Some(TextReply.fromList("hi")(replyToMessage = true))).assertEquals(
      List("respond", "remember", "rememberAnswer")
    )
  }

  test("a message the LLM was asked about and did not answer is remembered alone") {
    eventsOf("EngineBot: hello", None).assertEquals(List("respond", "remember"))
  }

  test("a plain message answered by a reactive reply, or not directed, is remembered without asking the LLM") {
    for {
      reactive   <- eventsOf("EngineBot: ping", None)
      undirected <- eventsOf("just chatting", None)
    } yield {
      assertEquals(reactive, List("remember"))
      assertEquals(undirected, List("remember"))
    }
  }

  test("a command is never remembered") {
    eventsOf("!llm", None).assertEquals(List.empty[String])
  }

  test("a failing responder or memory is logged by exception class, never by message") {
    val secret                    = "secret chat text"
    val failing: LlmResponder[IO] = new LlmResponder[IO] {
      override def respond(msg: Message): IO[Option[Reply]] =
        IO.raiseError(new RuntimeException(secret))
      override def remember(msg: Message)(using cats.Applicative[IO]): IO[Unit] =
        IO.raiseError(new IllegalStateException(secret))
    }
    for {
      lines <- Ref.of[IO, List[String]](List.empty)
      _     <- runAll(
        messages = List(message("EngineBot: hello")),
        responder = failing,
        log = recordingLog(lines),
        typingFailure = Some(new RuntimeException(secret))
      )
      seen <- lines.get
    } yield {
      assert(seen.exists(_.contains("typing action failed: RuntimeException")), seen.toString)
      assert(seen.exists(l => l.contains("the LLM") && l.contains("RuntimeException")), seen.toString)
      assert(seen.exists(l => l.contains("memory failed") && l.contains("IllegalStateException")), seen.toString)
      assert(seen.forall(!_.contains(secret)), seen.toString)
    }
  }

  test("a stalled memory is cut by the timeout and the message still completes") {
    val stalled: LlmResponder[IO] = new LlmResponder[IO] {
      override def respond(msg: Message): IO[Option[Reply]]                     = IO.pure(None)
      override def remember(msg: Message)(using cats.Applicative[IO]): IO[Unit] = IO.never
    }
    // onMessage must complete shortly after llmTimeout, not hang on the memory: the outer timeout fails the test
    runAll(messages = List(message("EngineBot: hello")), responder = stalled, llmTimeout = 50.millis)
      .timeout(5.seconds)
      .map(r => assertEquals(r.sent, List.empty[ReplyValue]))
  }

  test("a cancelled LLM call releases the chat: the next directed message is asked") {
    for {
      adapter <- FakePlatformAdapter()
      calls   <- Ref.of[IO, Int](0)
      hang: LlmResponder[IO] = _ => calls.update(_ + 1) *> IO.never
      engine                 = new BotEngine[IO](
        sBotConfig = sBotConfig,
        dbLayer = DBLayerMock.mock(sBotInfo.botId, messageReplies = messageReplies, llmSettings = settings(true)),
        filteringMatchesMessages = (_, _) => IO.pure(true),
        postComputation = _ => IO.unit,
        notifier = Notifier.noop[IO],
        backgroundJobManager = BackgroundJobManagerMock.mock(),
        llmResponder = hang,
        llmTimeout = 1.minute
      )
      _ <- engine.onMessage(message("EngineBot: one"))(using adapter).timeoutTo(50.millis, IO.unit)
      _ <- engine.onMessage(message("EngineBot: two"))(using adapter).timeoutTo(50.millis, IO.unit)
      n <- calls.get
    } yield assertEquals(n, 2)
  }

  test("a stalled settings lookup is cut by the stage timeout and the claim is released") {
    for {
      adapter <- FakePlatformAdapter()
      calls   <- Ref.of[IO, Int](0)
      stalled = new LlmSettings[IO] {
        def enable(chatId: ChatId): IO[Unit]               = IO.unit
        def disable(chatId: ChatId): IO[Unit]              = IO.unit
        def status(chatId: ChatId): IO[LlmSettings.Status] = calls.update(_ + 1) *> IO.never
        def forget(chatId: ChatId): IO[Unit]               = IO.unit
      }
      engine = new BotEngine[IO](
        sBotConfig = sBotConfig.copy(requestProcessingTimeout = Some(50.millis)),
        dbLayer = DBLayerMock.mock(sBotInfo.botId, messageReplies = messageReplies, llmSettings = stalled),
        filteringMatchesMessages = (_, _) => IO.pure(true),
        postComputation = _ => IO.unit,
        notifier = Notifier.noop[IO],
        backgroundJobManager = BackgroundJobManagerMock.mock(),
        llmResponder = llmAnswer,
        llmTimeout = 50.millis
      )
      _ <- engine.onMessage(message("EngineBot: one"))(using adapter).timeout(5.seconds)
      _ <- engine.onMessage(message("EngineBot: two"))(using adapter).timeout(5.seconds)
      n <- calls.get
    } yield assertEquals(n, 2) // both reached the lookup: the first released its claim when the stage timed out
  }

  test("a delivered answer is remembered even when the interaction update stalls and the stage is cut") {
    for {
      events <- Ref.of[IO, List[String]](List.empty)
      r      <- runAll(
        messages = List(message("EngineBot: hello")),
        responder = recording(events, Some(TextReply.fromList("hi")(replyToMessage = true))),
        llmTimeout = 50.millis,
        requestTimeout = Some(50.millis),
        stallInteraction = true
      ).timeout(5.seconds)
      seen <- events.get
    } yield {
      assertEquals(r.sent, List[ReplyValue](Text("hi")))
      assertEquals(seen, List("respond", "remember", "rememberAnswer"))
    }
  }
}
