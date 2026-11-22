package com.benkio.chatcore

import cats.effect.Clock
import cats.syntax.all.*
import cats.Monad
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.SubscriptionFlowKey
import com.benkio.chatcore.repository.db.DBSubscriptionFlowPrompt

import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.duration.FiniteDuration

/** The prompt message an adapter is showing for a guided subscription draft. The adapter needs it twice: to edit the
  * same message at the next step, and to check that the user pressing a button is the owner of the draft.
  */
final case class SubscriptionFlowPromptHandle(
    key: SubscriptionFlowKey,
    messageId: String,
    owner: String,
    savedAt: FiniteDuration
)

/** The prompt handles of an adapter. Entries expire with the draft idle timeout: after that a press on the old message
  * is answered by the adapter as a prompt that is not available anymore. When the bound is reached the oldest handle is
  * dropped, since a prompt is only a handle to a message already on the platform.
  */
trait SubscriptionFlowPromptStore[F[_]] {
  def put(key: SubscriptionFlowKey, messageId: String, owner: String): F[Unit]
  def touch(key: SubscriptionFlowKey): F[Unit]
  def byKey(key: SubscriptionFlowKey): F[Option[SubscriptionFlowPromptHandle]]
  def byMessageId(chatId: ChatId, messageId: String): F[Option[SubscriptionFlowPromptHandle]]
  def remove(key: SubscriptionFlowKey): F[Unit]
}

object SubscriptionFlowPromptStore {

  def create[F[_]: Monad: Clock](expiry: FiniteDuration, maxPrompts: Int): SubscriptionFlowPromptStore[F] =
    inMemory[F](expiry, maxPrompts)

  def inMemory[F[_]: Monad: Clock](expiry: FiniteDuration, maxPrompts: Int): SubscriptionFlowPromptStore[F] =
    new InMemorySubscriptionFlowPromptStore[F](expiry, maxPrompts)

  def db[F[_]](dbSubscriptionFlowPrompt: DBSubscriptionFlowPrompt[F]): SubscriptionFlowPromptStore[F] =
    new DbSubscriptionFlowPromptStore[F](dbSubscriptionFlowPrompt)

  private final class DbSubscriptionFlowPromptStore[F[_]](dbSubscriptionFlowPrompt: DBSubscriptionFlowPrompt[F])
      extends SubscriptionFlowPromptStore[F] {
    override def put(key: SubscriptionFlowKey, messageId: String, owner: String): F[Unit] =
      dbSubscriptionFlowPrompt.put(key, messageId, owner)
    override def touch(key: SubscriptionFlowKey): F[Unit] = dbSubscriptionFlowPrompt.touch(key)
    override def byKey(key: SubscriptionFlowKey): F[Option[SubscriptionFlowPromptHandle]] =
      dbSubscriptionFlowPrompt.byKey(key)
    override def byMessageId(chatId: ChatId, messageId: String): F[Option[SubscriptionFlowPromptHandle]] =
      dbSubscriptionFlowPrompt.byMessageId(chatId, messageId)
    override def remove(key: SubscriptionFlowKey): F[Unit] = dbSubscriptionFlowPrompt.remove(key)
  }

  /** In-memory implementation, kept for tests and local use. A restart forgets it. */
  private final class InMemorySubscriptionFlowPromptStore[F[_]: Monad: Clock](expiry: FiniteDuration, maxPrompts: Int)
      extends SubscriptionFlowPromptStore[F] {

    private type MessageRef = (ChatId, String)

    private final case class State(
        byKey: Map[SubscriptionFlowKey, SubscriptionFlowPromptHandle] = Map.empty,
        byMessageId: Map[MessageRef, SubscriptionFlowPromptHandle] = Map.empty
    )

    private val state = AtomicReference[State](State())

    private def expired(handle: SubscriptionFlowPromptHandle, now: FiniteDuration): Boolean =
      now - handle.savedAt >= expiry

    private def live(current: State, now: FiniteDuration): State = {
      val handles = current.byKey.values.filterNot(handle => expired(handle, now)).toList
      State(handles.map(h => h.key -> h).toMap, handles.map(h => (h.key.chatId, h.messageId) -> h).toMap)
    }

    private def bounded(current: State): State =
      if current.byKey.size <= maxPrompts then current
      else {
        val oldest = current.byKey.values.toList.sortBy(_.savedAt).head
        State(current.byKey - oldest.key, current.byMessageId - ((oldest.key.chatId, oldest.messageId)))
      }

    override def put(key: SubscriptionFlowKey, messageId: String, owner: String): F[Unit] =
      Clock[F].realTime.flatMap { now =>
        state.updateAndGet { previous =>
          val current   = live(previous, now)
          val handle    = SubscriptionFlowPromptHandle(key, messageId, owner, now)
          val ref       = (key.chatId, messageId)
          val staleRefs = current.byKey.get(key).map(h => (h.key.chatId, h.messageId)).toList
          bounded(
            State(
              byKey = current.byKey.updated(key, handle),
              byMessageId = (current.byMessageId -- (ref :: staleRefs)).updated(ref, handle)
            )
          )
        }
        Monad[F].unit
      }

    override def touch(key: SubscriptionFlowKey): F[Unit] =
      Clock[F].realTime.flatMap { now =>
        state.updateAndGet { previous =>
          val current = live(previous, now)
          current.byKey.get(key).fold(current) { handle =>
            val refreshed = handle.copy(savedAt = now)
            State(
              byKey = current.byKey.updated(key, refreshed),
              byMessageId = current.byMessageId.updated((handle.key.chatId, handle.messageId), refreshed)
            )
          }
        }
        Monad[F].unit
      }

    override def byKey(key: SubscriptionFlowKey): F[Option[SubscriptionFlowPromptHandle]] =
      Clock[F].realTime.map { now =>
        state.updateAndGet(current => live(current, now)).byKey.get(key)
      }

    override def byMessageId(chatId: ChatId, messageId: String): F[Option[SubscriptionFlowPromptHandle]] =
      Clock[F].realTime.map { now =>
        state.updateAndGet(current => live(current, now)).byMessageId.get((chatId, messageId))
      }

    override def remove(key: SubscriptionFlowKey): F[Unit] =
      Clock[F].realTime.flatMap { now =>
        state.updateAndGet { previous =>
          val current = live(previous, now)
          val stale   = current.byKey.get(key)
          State(
            byKey = current.byKey - key,
            byMessageId = stale.fold(current.byMessageId)(h => current.byMessageId - ((h.key.chatId, h.messageId)))
          )
        }
        Monad[F].unit
      }
  }
}
