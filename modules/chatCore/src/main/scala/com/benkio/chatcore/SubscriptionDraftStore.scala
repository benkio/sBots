package com.benkio.chatcore

import cats.effect.Clock
import cats.syntax.all.*
import cats.Monad
import com.benkio.chatcore.model.SubscriptionDraft
import com.benkio.chatcore.model.SubscriptionFlowKey
import com.benkio.chatcore.repository.db.DBSubscriptionDraft

import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.duration.*

/** Whether a new guided subscription draft found room in the store. */
enum SubscriptionDraftStartResult {
  case Started
  case Full
}

/** Whether a draft could be stored by a save. */
enum SubscriptionDraftPutResult {
  case Saved
  case Full
}

/** The guided subscription drafts of an engine. There is one draft per (chat, bot, requester), a new bare `/subscribe`
  * replaces the one of the same triple, and a draft is silently forgotten once idle for `expiry`. A full store never
  * evicts a draft to make room: the new flow is declined.
  */
trait SubscriptionDraftStore[F[_]] {
  def start(draft: SubscriptionDraft): F[SubscriptionDraftStartResult]
  def get(key: SubscriptionFlowKey): F[Option[SubscriptionDraft]]
  def put(draft: SubscriptionDraft): F[SubscriptionDraftPutResult]
  def remove(key: SubscriptionFlowKey): F[Unit]
  def size: F[Int]
}

object SubscriptionDraftStore {

  val defaultExpiry: FiniteDuration = 10.minutes
  val defaultMaxDrafts: Int         = 100

  def create[F[_]: Monad: Clock](expiry: FiniteDuration, maxDrafts: Int): SubscriptionDraftStore[F] =
    inMemory[F](expiry, maxDrafts)

  def inMemory[F[_]: Monad: Clock](expiry: FiniteDuration, maxDrafts: Int): SubscriptionDraftStore[F] =
    new InMemorySubscriptionDraftStore[F](expiry, maxDrafts)

  def db[F[_]](dbSubscriptionDraft: DBSubscriptionDraft[F]): SubscriptionDraftStore[F] =
    new DbSubscriptionDraftStore[F](dbSubscriptionDraft)

  private final class DbSubscriptionDraftStore[F[_]](dbSubscriptionDraft: DBSubscriptionDraft[F])
      extends SubscriptionDraftStore[F] {
    override def start(draft: SubscriptionDraft): F[SubscriptionDraftStartResult] = dbSubscriptionDraft.start(draft)
    override def get(key: SubscriptionFlowKey): F[Option[SubscriptionDraft]]      = dbSubscriptionDraft.get(key)
    override def put(draft: SubscriptionDraft): F[SubscriptionDraftPutResult]     = dbSubscriptionDraft.put(draft)
    override def remove(key: SubscriptionFlowKey): F[Unit]                        = dbSubscriptionDraft.remove(key)
    override def size: F[Int]                                                     = dbSubscriptionDraft.size
  }

  /** In-memory implementation, kept for tests and local use. A restart forgets it. */
  private final class InMemorySubscriptionDraftStore[F[_]: Monad: Clock](expiry: FiniteDuration, maxDrafts: Int)
      extends SubscriptionDraftStore[F] {

    private val drafts = AtomicReference[Map[SubscriptionFlowKey, SubscriptionDraft]](Map.empty)

    private def expired(draft: SubscriptionDraft, now: FiniteDuration): Boolean =
      now - draft.savedAt >= expiry

    private def live(current: Map[SubscriptionFlowKey, SubscriptionDraft], now: FiniteDuration) =
      current.filterNot { case (_, draft) => expired(draft, now) }

    override def start(draft: SubscriptionDraft): F[SubscriptionDraftStartResult] =
      Clock[F].realTime.map { now =>
        val committed = drafts.updateAndGet { current =>
          val currentLive = live(current, now)
          if currentLive.contains(draft.key) || currentLive.size < maxDrafts then currentLive.updated(draft.key, draft)
          else currentLive
        }
        if committed.contains(draft.key) then SubscriptionDraftStartResult.Started
        else SubscriptionDraftStartResult.Full
      }

    override def get(key: SubscriptionFlowKey): F[Option[SubscriptionDraft]] =
      Clock[F].realTime.map { now =>
        var found: Option[SubscriptionDraft] = None
        drafts.updateAndGet { current =>
          val currentLive = live(current, now)
          found = currentLive.get(key)
          currentLive
        }
        found
      }

    override def put(draft: SubscriptionDraft): F[SubscriptionDraftPutResult] =
      start(draft).map {
        case SubscriptionDraftStartResult.Started => SubscriptionDraftPutResult.Saved
        case SubscriptionDraftStartResult.Full    => SubscriptionDraftPutResult.Full
      }

    override def remove(key: SubscriptionFlowKey): F[Unit] =
      Clock[F].realTime.flatMap { now =>
        drafts.updateAndGet(current => live(current, now) - key).pure[F].void
      }

    override def size: F[Int] =
      Clock[F].realTime.map(now => drafts.updateAndGet(current => live(current, now)).size)
  }
}
