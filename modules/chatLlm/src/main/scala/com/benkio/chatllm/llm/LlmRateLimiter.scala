package com.benkio.chatllm.llm

import cats.effect.Async
import cats.effect.Clock
import cats.effect.Ref
import cats.syntax.all.*

import java.time.temporal.ChronoUnit
import java.time.Instant

/** Which of the two sliding windows stopped a completion. */
enum BudgetWindow {
  case Minute, Day
}

/** The outcome of a budget check. `firstNotice` is true only for the first denial after a period in which requests were
  * allowed: the ops notification must not be repeated for every message dropped while the quota is exhausted.
  */
enum RateLimitDecision {
  case Allowed
  case Exhausted(window: BudgetWindow, firstNotice: Boolean)
}

/** The shared client-side limiter: one instance is built at startup and used by every bot. Both windows slide, so a
  * burst at a window boundary cannot double the configured allowance. A completion reserves one slot; the provider
  * fallback of that completion is part of the same request and does not consume another one.
  */
trait LlmRateLimiter[F[_]] {

  /** Reserves one request at the current instant, or refuses it. The clock is read inside the atomic state update, so a
    * retried update filters the windows and records the admission with a fresh timestamp.
    */
  def acquire: F[RateLimitDecision]

  /** Deterministic variant of [[acquire]]: the time is passed in so the windows are testable without sleeping. */
  def acquire(at: Instant): F[RateLimitDecision]

  /** Called when the ops notification for the first denial failed: the next denial may notify again. */
  def notificationFailed: F[Unit]
}

object LlmRateLimiter {

  private final case class State(
      thisMinute: List[Long],
      today: List[Long],
      notified: Boolean
  )

  private val empty = State(Nil, Nil, notified = false)

  def apply[F[_]: Async](config: LlmBudgetConfig): F[LlmRateLimiter[F]] =
    Ref.of[F, State](empty).map(states => new Impl[F](states, config))

  private final class Impl[F[_]: Async](states: Ref[F, State], config: LlmBudgetConfig) extends LlmRateLimiter[F] {

    override def acquire: F[RateLimitDecision] = {
      // access + compare-and-set keeps the update atomic while allowing an effectful clock read: each retry reads a
      // fresh instant instead of reusing one captured before the update.
      def attempt: F[RateLimitDecision] =
        states.access.flatMap { case (state, set) =>
          Clock[F].realTimeInstant.flatMap { at =>
            val (next, decision) = decide(state, at)
            set(next).ifM(decision.pure[F], attempt)
          }
        }
      attempt
    }

    override def acquire(at: Instant): F[RateLimitDecision] =
      states.modify(state => decide(state, at))

    private def decide(state: State, at: Instant): (State, RateLimitDecision) = {
      val now      = at.toEpochMilli
      val minuteAt = at.minusSeconds(60).toEpochMilli
      val dayAt    = at.minus(1, ChronoUnit.DAYS).toEpochMilli
      val minute   = state.thisMinute.filter(_ > minuteAt)
      val today    = state.today.filter(_ > dayAt)

      if minute.size >= config.maxPerMinute then (
        state.copy(thisMinute = minute, today = today, notified = true),
        RateLimitDecision.Exhausted(BudgetWindow.Minute, firstNotice = !state.notified)
      )
      else if today.size >= config.maxPerDay then (
        state.copy(thisMinute = minute, today = today, notified = true),
        RateLimitDecision.Exhausted(BudgetWindow.Day, firstNotice = !state.notified)
      )
      else
        (
          State(now :: minute, now :: today, notified = false),
          RateLimitDecision.Allowed
        )
    }

    override def notificationFailed: F[Unit] =
      states.update(state => state.copy(notified = false))
  }
}
