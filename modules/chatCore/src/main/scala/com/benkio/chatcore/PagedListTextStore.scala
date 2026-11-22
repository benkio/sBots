package com.benkio.chatcore

import cats.Applicative
import com.benkio.chatcore.model.reply.PagedItem
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.repository.db.DBPagedListText

import scala.collection.mutable
import scala.concurrent.duration.*

/** The paged lists a platform without buttons showed in each chat: the basis of the text commands `!next`, `!prev` and
  * `!pick` (see [[PagedListTextCommands]]). An entry is the page as shown: the command of the list (run again for
  * another page), the page, the items of the page (the picks), and the message that asked for it (the same input the
  * command runs again with).
  *
  * An entry expires after [[PagedListTextStore.defaultExpiry]] (the `paged_list_text_mode_expiry` of the specs) from
  * when it was saved, and the store keeps at most [[PagedListTextStore.defaultMaxChats]] chats (the
  * `paged_list_text_mode_max_chats` of the specs): the oldest entries are dropped when the store is full.
  */
object PagedListTextStore {

  /** A remembered page: the command of the list, the page as shown and the items of the page. */
  final case class Entry(
      commandKey: CommandKey,
      page: Int,
      pageItems: List[PagedItem],
      request: Message,
      savedAt: FiniteDuration
  ) {
    def isExpired(now: FiniteDuration, expiry: FiniteDuration): Boolean = now - savedAt > expiry
  }

  trait Store[F[_]] {
    def save(
        chatId: ChatId,
        commandKey: CommandKey,
        page: Int,
        pageItems: List[PagedItem],
        request: Message,
        now: FiniteDuration
    ): F[Unit]
    def get(chatId: ChatId, now: FiniteDuration): F[Option[Entry]]
    def isExpired(chatId: ChatId, now: FiniteDuration): F[Boolean]
    def size: F[Int]
  }

  def inMemory[F[_]: Applicative](expiry: FiniteDuration, maxChats: Int): Store[F] =
    new InMemoryStore[F](expiry, maxChats)

  def db[F[_]](dbPagedListText: DBPagedListText[F]): Store[F] =
    new DbStore[F](dbPagedListText)

  private final class DbStore[F[_]](dbPagedListText: DBPagedListText[F]) extends Store[F] {
    override def save(
        chatId: ChatId,
        commandKey: CommandKey,
        page: Int,
        pageItems: List[PagedItem],
        request: Message,
        now: FiniteDuration
    ): F[Unit] = dbPagedListText.save(chatId, commandKey, page, pageItems, request, now)
    override def get(chatId: ChatId, now: FiniteDuration): F[Option[Entry]] = dbPagedListText.get(chatId, now)
    override def isExpired(chatId: ChatId, now: FiniteDuration): F[Boolean] = dbPagedListText.isExpired(chatId, now)
    override def size: F[Int]                                               = dbPagedListText.size
  }

  private final class InMemoryStore[F[_]: Applicative](expiry: FiniteDuration, maxChats: Int) extends Store[F] {

    private val entries = mutable.LinkedHashMap[ChatId, Entry]()

    override def save(
        chatId: ChatId,
        commandKey: CommandKey,
        page: Int,
        pageItems: List[PagedItem],
        request: Message,
        now: FiniteDuration
    ): F[Unit] = Applicative[F].pure {
      synchronized {
        val _ = entries.remove(chatId)
        entries(chatId) = Entry(commandKey, page, pageItems, request, now)
        while entries.size > maxChats do {
          val _ = entries.remove(entries.head._1)
        }
      }
    }

    override def get(chatId: ChatId, now: FiniteDuration): F[Option[Entry]] = Applicative[F].pure {
      synchronized(entries.get(chatId).filterNot(_.isExpired(now, expiry)))
    }

    override def isExpired(chatId: ChatId, now: FiniteDuration): F[Boolean] = Applicative[F].pure {
      synchronized(entries.get(chatId).exists(_.isExpired(now, expiry)))
    }

    override def size: F[Int] = Applicative[F].pure(synchronized(entries.size))
  }

  /** The `paged_list_text_mode_expiry` of the specs. */
  val defaultExpiry: FiniteDuration = 10.minutes

  /** The `paged_list_text_mode_max_chats` of the specs. */
  val defaultMaxChats: Int = 100
}
