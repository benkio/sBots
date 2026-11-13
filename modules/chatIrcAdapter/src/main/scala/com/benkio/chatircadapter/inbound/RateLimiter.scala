package com.benkio.chatircadapter.inbound

import cats.effect.Async
import cats.effect.Ref
import cats.effect.Clock
import cats.syntax.all.*

import scala.concurrent.duration.FiniteDuration

/** Lets through at most `maxMessages` for each key in a window of time: the first message of a key starts its window,
  * the ones after that are refused until it is over. A fixed window, so a key can have twice the messages across the
  * edge of two windows.
  */
final class RateLimiter[F[_]: Async] private (
    state: Ref[F, Map[String, RateLimiter.Window]],
    maxMessages: Int,
    window: FiniteDuration
) {

  /** Whether the message of the key is allowed: it counts as one when it is. */
  def allow(key: String): F[Boolean] =
    Clock[F].monotonic.flatMap(now =>
      state.modify { windows =>
        val active = windows.get(key).filter(w => now - w.start < window)
        val (updated, allowed) = active match {
          case Some(w) if w.count >= maxMessages => (w, false)
          case Some(w)                           => (w.copy(count = w.count + 1), true)
          case None                              => (RateLimiter.Window(now, 1), true)
        }
        // the finished windows are dropped when the map grows, so it does not keep every sender forever
        val kept = if windows.size > RateLimiter.cleanupThreshold then windows.filter((_, w) => now - w.start < window) else windows
        (kept.updated(key, updated), allowed)
      }
    )
}

object RateLimiter {

  private val cleanupThreshold: Int = 1000

  final case class Window(start: FiniteDuration, count: Int)

  def apply[F[_]: Async](maxMessages: Int, window: FiniteDuration): F[RateLimiter[F]] =
    Ref.of[F, Map[String, Window]](Map.empty).map(new RateLimiter[F](_, maxMessages, window))
}
