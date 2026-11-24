package com.benkio.chatllm.observability

import cats.effect.IO
import cats.effect.Ref
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.Logger.given
import com.benkio.chatllm.llm.*
import com.benkio.chatllm.prompt.*
import com.benkio.chatllm.repository.db.LlmContextMessage
import com.benkio.chatllm.LlmContext
import munit.CatsEffectSuite

// obligation: rule.LlmInteractionLogged, rule.LlmTraceStoredWhenEnabled
class LlmObservabilitySpec extends CatsEffectSuite {

  private val chatId = ChatId(Platform.Telegram, "42")
  private val botId  = SBotId("bot")
  private val msg    =
    Message(messageId = None, date = 0L, chatId = chatId, chatType = "private", text = Some("ciao bot"))
  private val greeting: ReplyBundleMessage = ReplyBundleMessage.textToText("ciao")("Buongiorno Signori.")
  private val catalogue                    = List(CatalogueEntry(ReplyCatalogue.idOf(greeting), "A «ciao» risponde."))

  private val context: LlmContext[IO] = new LlmContext[IO] {
    def remember(msg: Message, botId: SBotId): IO[Boolean]                       = IO.pure(false)
    def rememberAssistant(chatId: ChatId, botId: SBotId, c: String): IO[Boolean] = IO.pure(false)
    def window(chatId: ChatId, botId: SBotId): IO[List[LlmContextMessage]]       = IO.pure(Nil)
    def prompt(chatId: ChatId, botId: SBotId): IO[String]                        = IO.pure("ctx")
    def deleteExpired(nowEpochSecond: Long): IO[Int]                             = IO.pure(0)
  }

  private class MemoryTraces(ref: Ref[IO, List[LlmTrace]]) extends LlmTraceStore[IO] {
    def append(trace: LlmTrace): IO[Unit]                        = ref.update(_ :+ trace)
    def recent(chatHash: String, limit: Int): IO[List[LlmTrace]] = ref.get
    def deleteOlderThan(cutoff: Long): IO[Int]                   = IO.pure(0)
  }

  private def completion(
      status: CompletionStatus,
      reply: Option[LlmReply],
      attempts: List[ProviderAttempt],
      raw: Option[RawResponse] = None,
      rejection: Option[LocalRejection] = None
  ) =
    Completion("p", LlmProvider.Gemini, status, reply, attempts, raw, rejection)

  /** Runs the responder and gives back the log lines and the stored traces. */
  private def run(answer: Completion, traceEnabled: Boolean) =
    for {
      traces <- Ref[IO].of(List.empty[LlmTrace])
      observer = LlmObserver[IO](
        new MemoryTraces(traces),
        traceEnabled,
        "salt",
        { case LlmProvider.Gemini => Some("gemini-x"); case LlmProvider.OpenRouter => Some("or-y") }
      )
      builder   = PromptBuilder(500).fold(e => fail(e), identity)
      responder = new ChatLlmResponder[IO](
        client = new LlmClientApi[IO] { def complete(prompt: String): IO[Completion] = IO.pure(answer) },
        context = context,
        builder = builder,
        botId = botId,
        persona = "persona",
        catalogue = catalogue,
        observer = Some(observer)
      )
      _      <- responder.respond(msg, List(greeting))
      stored <- traces.get
    } yield stored

  private val ok = List(ProviderAttempt(LlmProvider.Gemini, Some(ProviderResult.Success)))

  test("the line carries the stable keys, the salted chat hash and no chat content") {
    val i = LlmInteraction(
      chatHash = LlmInteraction.chatHash(chatId, "salt"),
      bot = "bot",
      model = "gemini x",
      promptVersion = "v1",
      promptTokens = 10,
      responseTokens = 5,
      latencyMs = 123,
      status = "answered",
      action = ReplyAction.Reuse,
      reusedReplyId = Some("id1"),
      failure = None,
      attempts = List("Gemini=Success"),
      fallback = false,
      parsed = true,
      stripped = true,
      localRejection = None
    )
    val line = LlmInteractionLog.line(i)
    assert(line.startsWith("[llm] type=interaction chat="), line)
    assert(line.contains("model=gemini_x"), line)
    assert(line.contains("latency_ms=123 status=answered action=reuse reply_id=id1 failure=-"), line)
    assert(!line.contains("42"), "the raw chat id must not appear")
    assertNotEquals(LlmInteraction.chatHash(chatId, "salt"), LlmInteraction.chatHash(chatId, "other"))
  }

  test("the trace is stored with the full prompt and the raw response when enabled") {
    val raw = "```json\n{\"action\":\"text\",\"text\":\"Ciao!\"}\n```"
    run(
      completion(
        status = CompletionStatus.Answered,
        reply = Some(LlmReply("Ciao!", com.benkio.chatllm.llm.LlmAction.Text)),
        attempts = ok,
        raw = Some(RawResponse(LlmProvider.Gemini, raw, stripped = true))
      ),
      traceEnabled = true
    ).map { stored =>
      assertEquals(stored.size, 1)
      assertEquals(stored.head.response, Some(raw))
      assertEquals(stored.head.model, Some("gemini-x"))
      assertEquals(stored.head.promptVersion, "v1")
      assertEquals(stored.head.action, "text")
      assert(stored.head.prompt.contains("ciao bot") && stored.head.prompt.contains("persona"))
    }
  }

  test("nothing is stored when the trace is off") {
    run(
      completion(
        status = CompletionStatus.Answered,
        reply = Some(LlmReply("Ciao!", com.benkio.chatllm.llm.LlmAction.Text)),
        attempts = ok
      ),
      traceEnabled = false
    ).map(stored => assertEquals(stored, Nil))
  }

  test("failure reasons: local rejection, quota, provider result, rejected output") {
    val text = Some(LlmReply("/start", com.benkio.chatllm.llm.LlmAction.Text))
    def reason(c: Completion, d: LlmDecision, offered: Boolean = false) = LlmInteraction.failureOf(c, d, offered)
    assertEquals(
      reason(
        c = completion(
          status = CompletionStatus.Failed,
          reply = None,
          attempts = Nil,
          rejection = Some(LocalRejection.OversizedPrompt)
        ),
        d = LlmDecision.Silent
      ),
      Some("oversized_prompt")
    )
    assertEquals(
      reason(c = completion(status = CompletionStatus.Silent, reply = None, attempts = Nil), d = LlmDecision.Silent),
      Some("quota_exhausted")
    )
    assertEquals(
      reason(
        c = completion(
          status = CompletionStatus.Failed,
          reply = None,
          attempts = List(
            ProviderAttempt(LlmProvider.Gemini, Some(ProviderResult.ServerError)),
            ProviderAttempt(LlmProvider.OpenRouter, Some(ProviderResult.MalformedJson))
          )
        ),
        d = LlmDecision.Silent
      ),
      Some("malformedjson")
    )
    assertEquals(
      reason(c = completion(status = CompletionStatus.Answered, reply = text, attempts = ok), d = LlmDecision.Silent),
      Some("output_rejected")
    )
    assertEquals(
      reason(
        c = completion(status = CompletionStatus.Answered, reply = text, attempts = ok),
        d = LlmDecision.Text("x")
      ),
      None
    )
  }

  test("a fallback is reported when the other provider was asked") {
    val c = completion(
      status = CompletionStatus.Answered,
      reply = Some(LlmReply("x", com.benkio.chatllm.llm.LlmAction.Text)),
      attempts = List(
        ProviderAttempt(LlmProvider.Gemini, Some(ProviderResult.RateLimited)),
        ProviderAttempt(LlmProvider.OpenRouter, Some(ProviderResult.Success))
      )
    )
    assert(LlmInteraction.usedFallback(c))
    assertEquals(LlmInteraction.answeringProvider(c), Some(LlmProvider.OpenRouter))
    assert(!LlmInteraction.usedFallback(completion(status = CompletionStatus.Answered, reply = None, attempts = ok)))
  }

  test("the raw text and the stripping flag of a malformed reply are kept for the trace") {
    val started  = Completion.started("p", LlmProvider.Gemini)
    val response = ProviderResponse.malformedJson.copy(raw = Some("sure! {oops"), stripped = true)
    val after    = Completion.acceptResponse(started, LlmProvider.Gemini, response)
    assertEquals(after.raw, Some(RawResponse(LlmProvider.Gemini, "sure! {oops", stripped = true)))
  }

  test("a fallback failing without text does not take the blame for the primary's raw text") {
    val afterPrimary = Completion.acceptResponse(
      Completion.started("p", LlmProvider.Gemini),
      LlmProvider.Gemini,
      ProviderResponse.malformedJson.copy(raw = Some("garbage"))
    )
    val after = Completion.acceptResponse(afterPrimary, LlmProvider.OpenRouter, ProviderResponse.timeout)
    assertEquals(after.status, CompletionStatus.Failed)
    assertEquals(after.raw.map(_.provider), Some(LlmProvider.Gemini))
    assertEquals(LlmInteraction.responsibleProvider(after), Some(LlmProvider.Gemini))
    // without any raw text, the last provider asked is the one reported
    val noRaw = Completion.acceptResponse(
      Completion
        .acceptResponse(Completion.started("p", LlmProvider.Gemini), LlmProvider.Gemini, ProviderResponse.timeout),
      LlmProvider.OpenRouter,
      ProviderResponse.timeout
    )
    assertEquals(LlmInteraction.responsibleProvider(noRaw), Some(LlmProvider.OpenRouter))
  }

  test("the responder hands the observer the decision, the prompt version and whether the reuse was offered") {
    def observed(answer: Completion): IO[LlmObservation] =
      for {
        seen <- Ref[IO].of(List.empty[LlmObservation])
        observer  = new LlmObserver[IO] { def observe(o: LlmObservation): IO[Unit] = seen.update(_ :+ o) }
        responder = new ChatLlmResponder[IO](
          client = new LlmClientApi[IO] { def complete(prompt: String): IO[Completion] = IO.pure(answer) },
          context = context,
          builder = PromptBuilder(500).fold(e => fail(e), identity),
          botId = botId,
          persona = "persona",
          catalogue = catalogue,
          observer = Some(observer)
        )
        _   <- responder.respond(msg, List(greeting))
        all <- seen.get
      } yield all.head
    val reuse = (id: String) =>
      completion(
        status = CompletionStatus.Answered,
        reply = Some(LlmReply("", com.benkio.chatllm.llm.LlmAction.Reuse, Some(id))),
        attempts = ok
      )
    for {
      offered    <- observed(reuse(ReplyCatalogue.idOf(greeting)))
      notOffered <- observed(reuse("unknown"))
    } yield {
      assertEquals(offered.decision, LlmDecision.Reuse(ReplyCatalogue.idOf(greeting)))
      assert(offered.reuseOffered)
      assertEquals(offered.promptVersion, PromptBuilder.currentVersion)
      assertEquals(offered.botId, botId)
      assert(!notOffered.reuseOffered)
      assertEquals(LlmInteraction.actionOf(notOffered.decision, notOffered.reuseOffered)._1, ReplyAction.Silent)
    }
  }

  test("the trace TTL is validated") {
    val cfg = LlmConfig(
      gemini = ProviderConnection("https://g.example"),
      openrouter = ProviderConnection("https://o.example"),
      context = LlmContextConfig(authorSalt = Sensitive("s3cret"))
    )
    assert(
      LlmConfig
        .validate(cfg.copy(trace = LlmTraceConfig(ttl = scala.concurrent.duration.FiniteDuration(0, "seconds"))))
        .left
        .exists(_.contains("llm.trace.ttl"))
    )
    assertEquals(LlmConfig.validate(cfg), Right(()))
  }
}
