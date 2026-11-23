package com.benkio.chatllm.llm

import cats.effect.IO
import cats.syntax.all.*
import munit.CatsEffectSuite

import java.time.Instant

class LlmRateLimiterSpec extends CatsEffectSuite {

  private def limiter(
      perMinute: Int = 3,
      perDay: Int = 3
  ): IO[LlmRateLimiter[IO]] =
    LlmRateLimiter[IO](LlmBudgetConfig(maxPerMinute = perMinute, maxPerDay = perDay))

  private val start = Instant.ofEpochSecond(1_700_000_000L)

  test("acquire allows completions under both budgets") {
    limiter().flatMap { l =>
      for {
        first  <- l.acquire(start)
        second <- l.acquire(start.plusSeconds(1))
        third  <- l.acquire(start.plusSeconds(2))
      }
      yield {
        assertEquals(first, RateLimitDecision.Allowed)
        assertEquals(second, RateLimitDecision.Allowed)
        assertEquals(third, RateLimitDecision.Allowed)
      }
    }
  }

  test("acquire rejects completions over the global per-minute budget") {
    limiter(perMinute = 2).flatMap { l =>
      for {
        _     <- l.acquire(start)
        _     <- l.acquire(start.plusMillis(1))
        third <- l.acquire(start.plusMillis(2))
      }
      yield assertEquals(third, RateLimitDecision.Exhausted(BudgetWindow.Minute, firstNotice = true))
    }
  }

  test("acquire rejects completions over the global daily budget") {
    limiter(perMinute = 10, perDay = 2).flatMap { l =>
      for {
        _     <- l.acquire(start)
        _     <- l.acquire(start.plusSeconds(60))
        third <- l.acquire(start.plusSeconds(120))
      }
      yield assertEquals(third, RateLimitDecision.Exhausted(BudgetWindow.Day, firstNotice = true))
    }
  }

  test("acquire uses a sliding one-minute window") {
    limiter(perMinute = 2, perDay = 10).flatMap { l =>
      for {
        first  <- l.acquire(start)
        second <- l.acquire(start.plusSeconds(30))
        third  <- l.acquire(start.plusSeconds(45)) // rejected
        fourth <- l.acquire(start.plusSeconds(60)) // first slot left the window
      }
      yield {
        assertEquals(first, RateLimitDecision.Allowed)
        assertEquals(second, RateLimitDecision.Allowed)
        assertEquals(third, RateLimitDecision.Exhausted(BudgetWindow.Minute, firstNotice = true))
        assertEquals(fourth, RateLimitDecision.Allowed)
      }
    }
  }

  test("acquire notifies ops only on the first rejection for a window") {
    limiter(perMinute = 1, perDay = 10).flatMap { l =>
      for {
        _           <- l.acquire(start)
        firstLimit  <- l.acquire(start.plusMillis(1))
        secondLimit <- l.acquire(start.plusMillis(2))
      }
      yield {
        assertEquals(firstLimit, RateLimitDecision.Exhausted(BudgetWindow.Minute, firstNotice = true))
        assertEquals(secondLimit, RateLimitDecision.Exhausted(BudgetWindow.Minute, firstNotice = false))
      }
    }
  }

  test("acquire resets the notification after the rate limit window clears") {
    limiter(perMinute = 1, perDay = 10).flatMap { l =>
      for {
        _          <- l.acquire(start)
        firstLimit <- l.acquire(start.plusMillis(1))
        _          <- l.acquire(start.plusSeconds(61))
        nextLimit  <- l.acquire(start.plusSeconds(62))
      }
      yield {
        assertEquals(firstLimit, RateLimitDecision.Exhausted(BudgetWindow.Minute, firstNotice = true))
        assertEquals(nextLimit, RateLimitDecision.Exhausted(BudgetWindow.Minute, firstNotice = true))
      }
    }
  }

  test("acquire never exceeds the budget under concurrent calls") {
    val perMinute = 7
    limiter(perMinute = perMinute, perDay = 100).flatMap { l =>
      List
        .fill(30)(l.acquire(start))
        .parSequence
        .map(results => assertEquals(results.count(_ == RateLimitDecision.Allowed), perMinute))
    }
  }

  test("acquire counts completions globally, independent of the provider") {
    limiter(perMinute = 2, perDay = 10).flatMap { l =>
      for {
        first  <- l.acquire(start)
        second <- l.acquire(start.plusMillis(1))
        third  <- l.acquire(start.plusMillis(2))
      }
      yield {
        assertEquals(first, RateLimitDecision.Allowed)
        assertEquals(second, RateLimitDecision.Allowed)
        assertEquals(third, RateLimitDecision.Exhausted(BudgetWindow.Minute, firstNotice = true))
      }
    }
  }
}
