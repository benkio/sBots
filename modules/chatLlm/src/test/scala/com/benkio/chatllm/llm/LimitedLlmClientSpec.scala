package com.benkio.chatllm.llm

import cats.effect.IO
import cats.effect.Ref
import com.benkio.chatcore.mocks.NotifierMock
import munit.CatsEffectSuite
import pureconfig.ConfigSource

import scala.concurrent.duration.*

/** The client shared by all the bots: budget, silence on exhaustion, one ops message, timeout and token limits. */
class LimitedLlmClientSpec extends CatsEffectSuite {

  private val prompt = "ciao bot"

  private def config(extra: String): LlmConfig =
    ConfigSource
      .string(
        s"""gemini { endpoint = "https://gemini.example/generate", model = "gemini-test" }
           |openrouter { endpoint = "https://openrouter.example/chat", model = "openrouter-test" }
           |$extra""".stripMargin
      )
      .load[LlmConfig]
      .fold(err => throw new RuntimeException(err.prettyPrint()), identity)

  private def answered(prompt: String): Completion =
    Completion(
      prompt = prompt,
      primaryProvider = LlmProvider.Gemini,
      status = CompletionStatus.Answered,
      reply = Some(LlmReply("ok")),
      attempts = List(ProviderAttempt(LlmProvider.Gemini, Some(ProviderResult.Success)))
    )

  private def fakeClient(calls: Ref[IO, Int], delay: FiniteDuration): LlmClientApi[IO] =
    new LlmClientApi[IO] {
      override def complete(prompt: String): IO[Completion] =
        calls.update(_ + 1) *> IO.sleep(delay).as(answered(prompt))
    }

  private def build(
      calls: Ref[IO, Int],
      cfg: LlmConfig,
      notifier: com.benkio.chatcore.Notifier[IO],
      delay: FiniteDuration = 0.millis
  ): IO[LlmClientApi[IO]] =
    for {
      limiter <- LlmRateLimiter[IO](cfg.budget)
    } yield LimitedLlmClient[IO](fakeClient(calls, delay), cfg, limiter, notifier)

  test("the default config has a timeout, token limits and a shared budget") {
    val cfg = config("")
    assertEquals(cfg.timeout, 10.seconds)
    assertEquals(cfg.maxInputTokens, 4096)
    assertEquals(cfg.maxOutputTokens, 512)
    assertEquals(cfg.budget, LlmBudgetConfig(20, 50))
    assertEquals(LlmConfig.validate(cfg), Right(()))
  }

  test("invalid tuning is rejected instead of silently disabling a limit") {
    val cfg = config("timeout = 0 seconds\nmax-input-tokens = 0\nbudget {\nmax-per-day = -1\n}")
    assert(LlmConfig.validate(cfg).isLeft)
  }

  test("quota exhaustion stays silent and notifies ops only once") {
    val cfg = config("budget {\nmax-per-minute = 1\nmax-per-day = 1\n}")
    for {
      calls    <- Ref.of[IO, Int](0)
      notifier <- NotifierMock.recording
      client   <- build(calls, cfg, notifier)
      first    <- client.complete(prompt)
      second   <- client.complete(prompt)
      third    <- client.complete(prompt)
      count    <- calls.get
      infos    <- notifier.infos.get
    } yield {
      assertEquals(first.status, CompletionStatus.Answered)
      assertEquals(second.status, CompletionStatus.Silent)
      assertEquals(second.reply, None)
      assertEquals(second.attempts, Nil)
      assertEquals(third.status, CompletionStatus.Silent)
      // one client shared by all the bots: the single allowed request is the only provider call
      assertEquals(count, 1)
      assertEquals(infos.size, 1)
      assert(clue(infos.head._2).contains("quota exhausted"))
      assertEquals(infos.head._1, LimitedLlmClient.opsBotInfo)
    }
  }

  test("a prompt beyond the input limit fails without a provider call or an ops message") {
    val cfg = config("max-input-tokens = 2")
    for {
      calls    <- Ref.of[IO, Int](0)
      notifier <- NotifierMock.recording
      client   <- build(calls, cfg, notifier)
      result   <- client.complete("abcdefghij")
      next     <- client.complete(prompt)
      count    <- calls.get
      infos    <- notifier.infos.get
    } yield {
      assertEquals(result.status, CompletionStatus.Failed)
      assertEquals(result.attempts, Nil)
      // the refused prompt did not consume a budget slot: the next completion still reaches the provider
      assertEquals(next.status, CompletionStatus.Answered)
      assertEquals(count, 1)
      assertEquals(infos, Nil)
    }
  }

  test("a completion that exceeds the timeout fails instead of waiting forever") {
    val cfg = config("timeout = 10 milliseconds")
    for {
      calls    <- Ref.of[IO, Int](0)
      notifier <- NotifierMock.recording
      client   <- build(calls, cfg, notifier, delay = 200.millis)
      result   <- client.complete(prompt).attempt
      infos    <- notifier.infos.get
    } yield {
      assert(result.isLeft)
      assertEquals(infos, Nil)
    }
  }
}
