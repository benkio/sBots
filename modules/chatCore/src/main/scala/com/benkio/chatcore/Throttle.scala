package com.benkio.chatcore

import cats.effect.implicits.*
import cats.effect.Async
import cats.effect.Ref
import cats.implicits.*

import scala.concurrent.duration.FiniteDuration

/** Lets the first occurrence of a key through and counts the ones that follow in the window. When the window ends, a
  * summary of how many were suppressed is sent, so a repeated error is one message plus a count.
  *
  * @param awaitWindow
  *   completes when the window that started with the first occurrence is over
  */
final class Throttle[F[_]: Async] private (suppressed: Ref[F, Map[String, Int]], awaitWindow: F[Unit]) {

  /** Runs `send` if `key` is not in a window, otherwise counts it. `summary` gets the count at the end of a window
    * that suppressed something. The summary runs in the background: its failure is its own, `send` and the caller are
    * not affected.
    */
  def apply(key: String)(send: F[Unit], summary: Int => F[Unit]): F[Unit] =
    suppressed
      .modify(counts =>
        counts.get(key) match {
          case None        => (counts + (key -> 0), true)
          case Some(count) => (counts + (key -> (count + 1)), false)
        }
      )
      .flatMap(first =>
        Async[F].whenA(first)(
          send.guarantee(closeWindow(key, summary).start.void)
        )
      )

  private def closeWindow(key: String, summary: Int => F[Unit]): F[Unit] =
    awaitWindow >>
      suppressed.modify(counts => (counts - key, counts.getOrElse(key, 0))).flatMap(count =>
        summary(count).whenA(count > 0).attempt.void
      )
}

object Throttle {

  def apply[F[_]: Async](window: FiniteDuration): F[Throttle[F]] =
    withWindow(Async[F].sleep(window))

  /** The end of the window is up to the caller: for the tests. */
  private[chatcore] def withWindow[F[_]: Async](awaitWindow: F[Unit]): F[Throttle[F]] =
    Ref.of[F, Map[String, Int]](Map.empty).map(new Throttle[F](_, awaitWindow))
}
