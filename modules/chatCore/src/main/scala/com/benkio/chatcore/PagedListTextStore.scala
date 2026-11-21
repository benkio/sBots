package com.benkio.chatcore

import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.reply.PagedItem

import scala.collection.mutable
import scala.concurrent.duration.*

/** The paged lists a platform without buttons showed in each chat: the basis of the text commands `!next`, `!prev`
  * and `!pick` (see [[PagedListTextCommands]]). An entry is the page as shown: the command of the list (run again
  * for another page), the page, the items of the page (the picks), and the message that asked for it (the same input
  * the command runs again with).
  *
  * The store is per bot engine and memory only: a restart forgets it. An entry expires after
  * [[PagedListTextStore.defaultExpiry]] (the `paged_list_text_mode_expiry` of the specs) from when it was saved, and
  * the store keeps at most [[PagedListTextStore.defaultMaxChats]] chats (the `paged_list_text_mode_max_chats` of
  * the specs): the oldest entries are dropped when the store is full.
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

  final class Store(expiry: FiniteDuration, maxChats: Int) {

    private val entries = mutable.LinkedHashMap[ChatId, Entry]()

    /** Saves the page of the chat: a new entry replaces the old one of the chat; when the store is full to
      * `maxChats` chats the oldest entries are dropped.
      */
    def save(
        chatId: ChatId,
        commandKey: CommandKey,
        page: Int,
        pageItems: List[PagedItem],
        request: Message,
        now: FiniteDuration
    ): Unit = synchronized {
      val _ = entries.remove(chatId)
      entries(chatId) = Entry(commandKey, page, pageItems, request, now)
      while entries.size > maxChats do {
        val _ = entries.remove(entries.head._1)
      }
    }

    /** The entry of the chat, `None` when there is none or it expired. */
    def get(chatId: ChatId, now: FiniteDuration): Option[Entry] = synchronized {
      entries.get(chatId).filterNot(_.isExpired(now, expiry))
    }

    /** Whether the chat has an entry that is expired: the commands answer it differently from the chat without any
      * entry.
      */
    def isExpired(chatId: ChatId, now: FiniteDuration): Boolean = synchronized {
      entries.get(chatId).exists(_.isExpired(now, expiry))
    }

    def size: Int = synchronized(entries.size)
  }

  /** The `paged_list_text_mode_expiry` of the specs. */
  val defaultExpiry: FiniteDuration = 10.minutes

  /** The `paged_list_text_mode_max_chats` of the specs. */
  val defaultMaxChats: Int = 100
}
