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

/** The client shared by all the bots: local refusals, one ops message per silent exhaustion episode, unexpected-error
  * reporting and token limits. Provider quotas and the fallback belong to the underlying client.
  */
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

  private def configWithBlankKeys: LlmConfig =
    ConfigSource
      .string(
        s"""gemini { endpoint = "https://gemini.example/generate", model = "gemini-test", api-key = "   " }
           |openrouter { endpoint = "https://openrouter.example/chat", model = "openrouter-test", api-key = "  " }""".stripMargin
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

  private def countingClient(calls: Ref[IO, Int], delay: FiniteDuration): LlmClientApi[IO] =
    new LlmClientApi[IO] {
      override def complete(prompt: String): IO[Completion] =
        calls.update(_ + 1) *> IO.sleep(delay).as(answered(prompt))
    }

  private val silentClient: LlmClientApi[IO] =
    new LlmClientApi[IO] {
      override def complete(prompt: String): IO[Completion] =
        IO.pure(Completion.notRequested(prompt, LlmProvider.Gemini, CompletionStatus.Silent))
    }

  private def failingClient(error: Throwable): LlmClientApi[IO] =
    new LlmClientApi[IO] {
      override def complete(prompt: String): IO[Completion] = IO.raiseError(error)
    }

  private def build(
      underlying: LlmClientApi[IO],
      cfg: LlmConfig,
      notifier: Notifier[IO]
  ): IO[LlmClientApi[IO]] =
    LlmRateLimiter[IO](LlmConfig.quotaFor(cfg)).map(limiter => LimitedLlmClient[IO](underlying, cfg, limiter, notifier))

  private def build(
      calls: Ref[IO, Int],
      cfg: LlmConfig,
      notifier: Notifier[IO],
      delay: FiniteDuration = 0.millis
  ): IO[LlmClientApi[IO]] =
    build(countingClient(calls, delay), cfg, notifier)

  test("the default config has a timeout, token limits and per-provider quotas") {
    val cfg = config("")
    assertEquals(cfg.timeout, 10.seconds)
    assertEquals(cfg.maxInputTokens, 4096)
    assertEquals(cfg.maxOutputTokens, 512)
    assertEquals(cfg.gemini.quota, LlmBudgetConfig(20, 50))
    assertEquals(cfg.openrouter.quota, LlmBudgetConfig(20, 50))
    assertEquals(LlmConfig.validate(cfg), Right(()))
  }

  test("invalid tuning, including a provider quota, is rejected instead of silently disabling a limit") {
    val cfg = config("timeout = 0 seconds\nmax-input-tokens = 0\ngemini.quota.max-per-day = -1\n")
    assert(LlmConfig.validate(cfg).isLeft)
  }

  test("a silent completion stays silent and notifies ops only once per episode") {
    val cfg = config("")
    for {
      notifier <- NotifierMock.recording
      client   <- build(silentClient, cfg, notifier)
      first    <- client.complete(prompt)
      second   <- client.complete(prompt)
      infos    <- notifier.infos.get
    } yield {
      assertEquals(first.status, CompletionStatus.Silent)
      assertEquals(first.reply, None)
      assertEquals(first.attempts, Nil)
      assertEquals(second.status, CompletionStatus.Silent)
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
      // the refused prompt did not consume a quota slot: the next completion still reaches the provider
      assertEquals(next.status, CompletionStatus.Answered)
      assertEquals(count, 1)
      assertEquals(infos, Nil)
    }
  }

  test("an answered completion produces no ops message") {
    for {
      calls    <- Ref.of[IO, Int](0)
      notifier <- NotifierMock.recording
      client   <- build(countingClient(calls, 0.millis), config(""), notifier)
      result   <- client.complete(prompt)
      infos    <- notifier.infos.get
    } yield {
      assertEquals(result.status, CompletionStatus.Answered)
      assertEquals(infos, Nil)
    }
  }

  test("an unexpected provider error is reported to ops and propagated") {
    // obligation: invariant.UnexpectedErrorsReported
    val cfg   = config("")
    val error = new RuntimeException("boom")
    for {
      notifier <- NotifierMock.recording
      client   <- build(failingClient(error), cfg, notifier)
      result   <- client.complete(prompt).attempt
      errors   <- notifier.errors.get
      infos    <- notifier.infos.get
    } yield {
      assertEquals(result, Left(error))
      assertEquals(errors.map(_._1), List(LimitedLlmClient.opsBotInfo))
      assert(clue(errors.head._2).contains("LLM completion failed"))
      assertEquals(errors.head._3, error)
      // an unexpected error is not a quota notice
      assertEquals(infos, Nil)
    }
  }

  test("missing provider credentials fail locally without a provider call or an ops message") {
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

  test("blank provider credentials fail locally without a provider call or an ops message") {
    val cfg = configWithBlankKeys
    for {
      calls    <- Ref.of[IO, Int](0)
      notifier <- NotifierMock.recording
      client   <- build(calls, cfg, notifier)
      result   <- client.complete(prompt)
      count    <- calls.get
      infos    <- notifier.infos.get
    } yield {
      assert(!LlmConfig.credentialsComplete(cfg))
      assertEquals(result.status, CompletionStatus.Failed)
      assertEquals(count, 0)
      assertEquals(infos, Nil)
    }
  }

  test("a failed quota notification is retried on the next silent completion") {
    for {
      attempts <- Ref.of[IO, Int](0)
      notifier = failingNotifier(attempts)
      client   <- build(silentClient, config(""), notifier)
      first    <- client.complete(prompt)
      second   <- client.complete(prompt)
      notified <- attempts.get
    } yield {
      assertEquals(first.status, CompletionStatus.Silent)
      assertEquals(second.status, CompletionStatus.Silent)
      assertEquals(notified, 2)
    }
  }
}
