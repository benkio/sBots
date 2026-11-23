package com.benkio.chatllm.llm

import cats.effect.IO
import cats.syntax.all.*
import munit.CatsEffectSuite

import java.time.Instant

class LlmRateLimiterSpec extends CatsEffectSuite {

  private def limiter(
      gemini: LlmBudgetConfig = LlmBudgetConfig(maxPerMinute = 3, maxPerDay = 3),
      openrouter: LlmBudgetConfig = LlmBudgetConfig(maxPerMinute = 3, maxPerDay = 3)
  ): IO[LlmRateLimiter[IO]] =
    LlmRateLimiter[IO] {
      case LlmProvider.Gemini     => gemini
      case LlmProvider.OpenRouter => openrouter
    }

  private val start = Instant.ofEpochSecond(1_700_000_000L)

  test("acquire allows calls under both of a provider's budgets") {
    limiter().flatMap { l =>
      for {
        first  <- l.acquire(LlmProvider.Gemini, start)
        second <- l.acquire(LlmProvider.Gemini, start.plusSeconds(1))
        third  <- l.acquire(LlmProvider.Gemini, start.plusSeconds(2))
      } yield {
        assertEquals(first, RateLimitDecision.Allowed)
        assertEquals(second, RateLimitDecision.Allowed)
        assertEquals(third, RateLimitDecision.Allowed)
      }
    }
  }

  test("acquire rejects calls over a provider's per-minute budget") {
    limiter(gemini = LlmBudgetConfig(maxPerMinute = 2, maxPerDay = 10)).flatMap { l =>
      for {
        _     <- l.acquire(LlmProvider.Gemini, start)
        _     <- l.acquire(LlmProvider.Gemini, start.plusMillis(1))
        third <- l.acquire(LlmProvider.Gemini, start.plusMillis(2))
      } yield assertEquals(third, RateLimitDecision.Exhausted(BudgetWindow.Minute))
    }
  }

  test("acquire rejects calls over a provider's daily budget") {
    limiter(gemini = LlmBudgetConfig(maxPerMinute = 10, maxPerDay = 2)).flatMap { l =>
      for {
        _     <- l.acquire(LlmProvider.Gemini, start)
        _     <- l.acquire(LlmProvider.Gemini, start.plusSeconds(60))
        third <- l.acquire(LlmProvider.Gemini, start.plusSeconds(120))
      } yield assertEquals(third, RateLimitDecision.Exhausted(BudgetWindow.Day))
    }
  }

  test("acquire uses a sliding one-minute window per provider") {
    limiter(gemini = LlmBudgetConfig(maxPerMinute = 2, maxPerDay = 10)).flatMap { l =>
      for {
        first  <- l.acquire(LlmProvider.Gemini, start)
        second <- l.acquire(LlmProvider.Gemini, start.plusSeconds(30))
        third  <- l.acquire(LlmProvider.Gemini, start.plusSeconds(45)) // rejected
        fourth <- l.acquire(LlmProvider.Gemini, start.plusSeconds(60)) // first slot left the window
      } yield {
        assertEquals(first, RateLimitDecision.Allowed)
        assertEquals(second, RateLimitDecision.Allowed)
        assertEquals(third, RateLimitDecision.Exhausted(BudgetWindow.Minute))
        assertEquals(fourth, RateLimitDecision.Allowed)
      }
    }
  }

  test("each provider has its own quota and counters") {
    limiter(
      gemini = LlmBudgetConfig(maxPerMinute = 1, maxPerDay = 10),
      openrouter = LlmBudgetConfig(maxPerMinute = 2, maxPerDay = 10)
    ).flatMap { l =>
      for {
        g1 <- l.acquire(LlmProvider.Gemini, start)
        g2 <- l.acquire(LlmProvider.Gemini, start.plusMillis(1))     // gemini exhausted
        o1 <- l.acquire(LlmProvider.OpenRouter, start)
        o2 <- l.acquire(LlmProvider.OpenRouter, start.plusMillis(1))
        o3 <- l.acquire(LlmProvider.OpenRouter, start.plusMillis(2)) // openrouter exhausted
      } yield {
        assertEquals(g1, RateLimitDecision.Allowed)
        assertEquals(g2, RateLimitDecision.Exhausted(BudgetWindow.Minute))
        assertEquals(o1, RateLimitDecision.Allowed)
        assertEquals(o2, RateLimitDecision.Allowed)
        assertEquals(o3, RateLimitDecision.Exhausted(BudgetWindow.Minute))
      }
    }
  }

  test("the notice is claimed once across both providers until a provider is admitted again") {
    limiter(gemini = LlmBudgetConfig(maxPerMinute = 1, maxPerDay = 10)).flatMap { l =>
      for {
        _      <- l.acquire(LlmProvider.Gemini, start)
        _      <- l.acquire(LlmProvider.Gemini, start.plusMillis(1)) // exhausted
        first  <- l.claimQuotaNotice
        second <- l.claimQuotaNotice
        _      <- l.acquire(LlmProvider.OpenRouter, start)           // admitted on the other provider
        next   <- l.claimQuotaNotice
      } yield {
        assertEquals(first, true)
        assertEquals(second, false)
        assertEquals(next, true)
      }
    }
  }

  test("a failed notification resets the notice claim") {
    limiter(gemini = LlmBudgetConfig(maxPerMinute = 1, maxPerDay = 10)).flatMap { l =>
      for {
        _      <- l.acquire(LlmProvider.Gemini, start)
        _      <- l.acquire(LlmProvider.Gemini, start.plusMillis(1))
        first  <- l.claimQuotaNotice
        _      <- l.notificationFailed
        second <- l.claimQuotaNotice
      } yield {
        assertEquals(first, true)
        assertEquals(second, true)
      }
    }
  }

  test("acquire never exceeds a provider's budget under concurrent calls") {
    val perMinute = 7
    limiter(gemini = LlmBudgetConfig(maxPerMinute = perMinute, maxPerDay = 100)).flatMap { l =>
      List
        .fill(30)(l.acquire(LlmProvider.Gemini, start))
        .parSequence
        .map(results => assertEquals(results.count(_ == RateLimitDecision.Allowed), perMinute))
    }
  }

  test("acquire reads the current clock inside the atomic update") {
    limiter(gemini = LlmBudgetConfig(maxPerMinute = 1, maxPerDay = 10)).flatMap { l =>
      for {
        first  <- l.acquire(LlmProvider.Gemini)
        second <- l.acquire(LlmProvider.Gemini)
      } yield {
        assertEquals(first, RateLimitDecision.Allowed)
        assertEquals(second, RateLimitDecision.Exhausted(BudgetWindow.Minute))
      }
    }
  }
}
