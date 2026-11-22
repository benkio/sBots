package com.benkio.chatcore

import cats.effect.Clock
import cats.syntax.all.*
import cats.Monad
import com.benkio.chatcore.model.SubscriptionDraft
import com.benkio.chatcore.model.SubscriptionFlowKey

import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.duration.*

/** Whether a new guided subscription draft found room in the store. */
enum SubscriptionDraftStartResult {
  case Started
  case Full
}

/** The guided subscription drafts of an engine, in memory: a restart forgets them. There is one draft per (chat, bot,
  * requester), a new bare `/subscribe` replaces the one of the same triple, and a draft is silently forgotten once idle
  * for `expiry`. A full store never evicts a draft to make room: the new flow is declined.
  */
final class SubscriptionDraftStore[F[_]: Monad: Clock](expiry: FiniteDuration, maxDrafts: Int) {

  private val drafts = AtomicReference[Map[SubscriptionFlowKey, SubscriptionDraft]](Map.empty)

  private def expired(draft: SubscriptionDraft, now: FiniteDuration): Boolean =
    now - draft.savedAt >= expiry

  private def live(current: Map[SubscriptionFlowKey, SubscriptionDraft], now: FiniteDuration) =
    current.filterNot { case (_, draft) => expired(draft, now) }

  /** Starts or restarts the draft of a triple; `Full` when that triple has no draft and the store is at its bound.
    * `updateAndGet` may run the update more than once under contention, so the result is read from the map it actually
    * committed: a side effect on an earlier attempt would describe a map that was never installed. The full branch
    * leaves the key out, so membership in the committed map tells the two outcomes apart.
    */
  def start(draft: SubscriptionDraft): F[SubscriptionDraftStartResult] =
    Clock[F].realTime.map { now =>
      val committed = drafts.updateAndGet { current =>
        val currentLive = live(current, now)
        if currentLive.contains(draft.key) || currentLive.size < maxDrafts then currentLive.updated(draft.key, draft)
        else currentLive
      }
      if committed.contains(draft.key) then SubscriptionDraftStartResult.Started
      else SubscriptionDraftStartResult.Full
    }

  def get(key: SubscriptionFlowKey): F[Option[SubscriptionDraft]] =
    Clock[F].realTime.map { now =>
      var found: Option[SubscriptionDraft] = None
      drafts.updateAndGet { current =>
        val currentLive = live(current, now)
        found = currentLive.get(key)
        currentLive
      }
      found
    }

  /** Saves a draft after a valid answer or a rejection that keeps the user on the same step. */
  def put(draft: SubscriptionDraft): F[Unit] =
    Clock[F].realTime.flatMap { now =>
      drafts.updateAndGet(current => live(current, now).updated(draft.key, draft)).pure[F].void
    }

  def remove(key: SubscriptionFlowKey): F[Unit] =
    Clock[F].realTime.flatMap { now =>
      drafts.updateAndGet(current => live(current, now) - key).pure[F].void
    }

  def size: F[Int] =
    Clock[F].realTime.map(now => drafts.updateAndGet(current => live(current, now)).size)
}

object SubscriptionDraftStore {

  val defaultExpiry: FiniteDuration = 10.minutes
  val defaultMaxDrafts: Int         = 100

  def create[F[_]: Monad: Clock](expiry: FiniteDuration, maxDrafts: Int): SubscriptionDraftStore[F] =
    new SubscriptionDraftStore[F](expiry, maxDrafts)
}
