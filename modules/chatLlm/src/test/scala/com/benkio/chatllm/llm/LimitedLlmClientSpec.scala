package com.benkio.chatllm.llm

import cats.effect.IO
import cats.effect.Ref
import com.benkio.chatcore.mocks.NotifierMock
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Notifier
import munit.CatsEffectSuite
import pureconfig.ConfigSource

import scala.concurrent.duration.*

/** The client shared by all the bots: budget, silence on exhaustion, one ops message, timeout and token limits. */
class LimitedLlmClientSpec extends CatsEffectSuite {

  private val prompt = "ciao bot"

  private def config(extra: String): LlmConfig =
    ConfigSource
      .string(
        s"""gemini { endpoint = "https://gemini.example/generate", model = "gemini-test", api-key = "gemini-key" }
           |openrouter { endpoint = "https://openrouter.example/chat", model = "openrouter-test", api-key = "openrouter-key" }
           |$extra""".stripMargin
      )
      .load[LlmConfig]
      .fold(err => throw new RuntimeException(err.prettyPrint()), identity)

  private def configWithoutGeminiKey: LlmConfig =
    ConfigSource
      .string(
        s"""gemini { endpoint = "https://gemini.example/generate", model = "gemini-test" }
           |openrouter { endpoint = "https://openrouter.example/chat", model = "openrouter-test", api-key = "openrouter-key" }""".stripMargin
      )
      .load[LlmConfig]
      .fold(err => throw new RuntimeException(err.prettyPrint()), identity)

  private def failingNotifier(attempts: Ref[IO, Int]): Notifier[IO] =
    new Notifier[IO] {
      override def info(sBotInfo: SBotInfo, message: String): IO[Unit] =
        attempts.update(_ + 1) *> IO.raiseError(new RuntimeException("notification failed"))
      override def mediaError(message: Message, mediaFile: MediaFile, error: Throwable): IO[Unit] = IO.unit
      override def error(sBotInfo: SBotInfo, context: String, error: Throwable): IO[Unit]         = IO.unit
    }

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

  private def failingClient(error: Throwable): LlmClientApi[IO] =
    new LlmClientApi[IO] {
      override def complete(prompt: String): IO[Completion] = IO.raiseError(error)
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

  test("an unexpected provider error is reported to ops and propagated") {
    // obligation: invariant.UnexpectedErrorsReported
    val cfg   = config("")
    val error = new RuntimeException("boom")
    for {
      limiter  <- LlmRateLimiter[IO](cfg.budget)
      notifier <- NotifierMock.recording
      client = LimitedLlmClient[IO](failingClient(error), cfg, limiter, notifier)
      result <- client.complete(prompt).attempt
      errors <- notifier.errors.get
      infos  <- notifier.infos.get
    } yield {
      assertEquals(result, Left(error))
      assertEquals(errors.map(_._1), List(LimitedLlmClient.opsBotInfo))
      assert(clue(errors.head._2).contains("LLM completion failed"))
      assertEquals(errors.head._3, error)
      // an unexpected error is not a quota notice
      assertEquals(infos, Nil)
    }
  }

  test("missing provider credentials fail locally without a provider call or a budget slot") {
    val cfg = configWithoutGeminiKey
    for {
      calls    <- Ref.of[IO, Int](0)
      notifier <- NotifierMock.recording
      client   <- build(calls, cfg, notifier)
      first    <- client.complete(prompt)
      second   <- client.complete(prompt)
      count    <- calls.get
      infos    <- notifier.infos.get
    } yield {
      assertEquals(first.status, CompletionStatus.Failed)
      assertEquals(second.status, CompletionStatus.Failed)
      assertEquals(count, 0)
      assertEquals(infos, Nil)
    }
  }

  test("a failed quota notification is retried on the next denial") {
    val cfg = config("budget {\nmax-per-minute = 1\nmax-per-day = 1\n}")
    for {
      attempts <- Ref.of[IO, Int](0)
      calls    <- Ref.of[IO, Int](0)
      client   <- build(calls, cfg, failingNotifier(attempts))
      _        <- client.complete(prompt)
      second   <- client.complete(prompt)
      third    <- client.complete(prompt)
      notified <- attempts.get
    } yield {
      assertEquals(second.status, CompletionStatus.Silent)
      assertEquals(third.status, CompletionStatus.Silent)
      assertEquals(notified, 2)
    }
  }
}
