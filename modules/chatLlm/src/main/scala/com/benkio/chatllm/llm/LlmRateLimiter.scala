package com.benkio.chatllm.llm

import cats.effect.Async
import cats.effect.Clock
import cats.effect.Ref
import cats.syntax.all.*

import java.time.temporal.ChronoUnit
import java.time.Instant
import scala.concurrent.duration.FiniteDuration

/** Which of the two sliding windows stopped a provider call. */
enum BudgetWindow {
  case Minute, Day
}

/** The outcome of a provider quota check. The one-shot ops notice is tracked separately by the limiter, so a denial
  * only needs to report which window was exhausted.
  */
enum RateLimitDecision {
  case Allowed
  case Exhausted(window: BudgetWindow)
}

/** The per-provider client-side limiter: one instance is built at startup and shared by every bot. Each provider has
  * its own pair of sliding windows, so a fallback attempt consumes the fallback provider's quota, never the primary's.
  * A notification episode is shared across both providers: it is claimed once when no provider can be called and resets
  * as soon as any provider is admitted again.
  */
trait LlmRateLimiter[F[_]] {

  /** Reserves one call to `provider` at the current instant, or refuses it. The clock is read inside the atomic state
    * update, so a retried update filters the windows and records the admission with a fresh timestamp.
    */
  def acquire(provider: LlmProvider): F[RateLimitDecision]

  /** Deterministic variant of [[acquire]]: the time is passed in so the windows are testable without sleeping. */
  def acquire(provider: LlmProvider, at: Instant): F[RateLimitDecision]

  /** Atomically claims the one-shot exhaustion notice. Returns true only for the first caller of an episode. */
  def claimQuotaNotice: F[Boolean]

  /** Called when the ops notification for the episode failed: the next denial may notify again. */
  def notificationFailed: F[Unit]

  /** One-shot notices by key (for example a rejected provider or an oversized prompt): true only for the first caller
    * until the key is released. Independent from the quota notice.
    */
  def claimNotice(key: String): F[Boolean] = claimNotice(key, None)

  /** With a cooldown the key also expires by itself: a claim is granted again once the cooldown has elapsed, so a
    * condition that comes and goes cannot notify on every occurrence.
    */
  def claimNotice(key: String, cooldown: Option[FiniteDuration]): F[Boolean]

  /** Releases a key: the condition is over (or its notification failed), so the next occurrence may notify again. */
  def releaseNotice(key: String): F[Unit]
}

object LlmRateLimiter {

  private final case class ProviderWindow(thisMinute: List[Long], today: List[Long])

  private final case class State(
      gemini: ProviderWindow,
      openrouter: ProviderWindow,
      notified: Boolean,
      notices: Map[String, Long] = Map.empty
  )

  private val empty = State(ProviderWindow(Nil, Nil), ProviderWindow(Nil, Nil), notified = false)

  /** `quotaFor` supplies each provider's configured limits. */
  def apply[F[_]: Async](quotaFor: LlmProvider => LlmBudgetConfig): F[LlmRateLimiter[F]] =
    Ref.of[F, State](empty).map(states => new Impl[F](states, quotaFor))

  private final class Impl[F[_]: Async](states: Ref[F, State], quotaFor: LlmProvider => LlmBudgetConfig)
      extends LlmRateLimiter[F] {

    override def acquire(provider: LlmProvider): F[RateLimitDecision] = {
      // access + compare-and-set keeps the update atomic while allowing an effectful clock read: each retry reads a
      // fresh instant instead of reusing one captured before the update.
      def attempt: F[RateLimitDecision] =
        states.access.flatMap { case (state, set) =>
          Clock[F].realTimeInstant.flatMap { at =>
            val (next, decision) = decide(state = state, provider = provider, at = at)
            set(next).ifM(decision.pure[F], attempt)
          }
        }
      attempt
    }

    override def acquire(provider: LlmProvider, at: Instant): F[RateLimitDecision] =
      states.modify(state => decide(state = state, provider = provider, at = at))

    private def decide(state: State, provider: LlmProvider, at: Instant): (State, RateLimitDecision) = {
      val now      = at.toEpochMilli
      val minuteAt = at.minusSeconds(60).toEpochMilli
      val dayAt    = at.minus(1, ChronoUnit.DAYS).toEpochMilli
      val current  = windowOf(state, provider)
      val minute   = current.thisMinute.filter(_ > minuteAt)
      val today    = current.today.filter(_ > dayAt)
      val quota    = quotaFor(provider)

      def withWindow(window: ProviderWindow, notified: Boolean = state.notified): State =
        provider match {
          case LlmProvider.Gemini     => state.copy(gemini = window, notified = notified)
          case LlmProvider.OpenRouter => state.copy(openrouter = window, notified = notified)
        }

      if minute.size >= quota.maxPerMinute then (
        withWindow(ProviderWindow(minute, today)),
        RateLimitDecision.Exhausted(BudgetWindow.Minute)
      )
      else if today.size >= quota.maxPerDay then (
        withWindow(ProviderWindow(minute, today)),
        RateLimitDecision.Exhausted(BudgetWindow.Day)
      )
      else {
        // A provider was admitted: this starts a new notification episode.
        (
          withWindow(ProviderWindow(now :: minute, now :: today), notified = false),
          RateLimitDecision.Allowed
        )
      }
    }

    private def windowOf(state: State, provider: LlmProvider): ProviderWindow =
      provider match {
        case LlmProvider.Gemini     => state.gemini
        case LlmProvider.OpenRouter => state.openrouter
      }

    override val claimQuotaNotice: F[Boolean] =
      states.modify(state =>
        if state.notified then (state, false)
        else (state.copy(notified = true), true)
      )

    override def claimNotice(key: String, cooldown: Option[FiniteDuration]): F[Boolean] =
      Async[F].realTime.flatMap { now =>
        val nowMillis = now.toMillis
        states.modify { state =>
          val active = state.notices.get(key).exists(at => cooldown.forall(c => nowMillis - at < c.toMillis))
          if active then (state, false) else (state.copy(notices = state.notices.updated(key, nowMillis)), true)
        }
      }

    override def releaseNotice(key: String): F[Unit] =
      states.update(state => state.copy(notices = state.notices - key))

    override def notificationFailed: F[Unit] =
      states.update(state => state.copy(notified = false))
  }
}
