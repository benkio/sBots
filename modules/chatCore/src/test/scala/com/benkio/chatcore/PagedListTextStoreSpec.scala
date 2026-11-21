package com.benkio.chatcore

import com.benkio.chatcore.model.reply.PagedItem
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.TestIds
import munit.FunSuite

import scala.concurrent.duration.*

class PagedListTextStoreSpec extends FunSuite {

  private val expiry = 10.minutes

  private def newStore(): PagedListTextStore.Store = new PagedListTextStore.Store(expiry, maxChats = 2)

  private def request(chatId: ChatId, text: String): Message =
    Message(None, 0L, chatId, "private", text = Some(text))

  private def pageItems(count: Int): List[PagedItem] =
    (1 to count).toList.map(n => PagedItem.ValueItem(Text(s"item $n")))

  test("a chat has one entry at most: a new entry replaces the old one") {
    val store = newStore()
    val chat  = TestIds.chatId(1L)
    store.save(chat, CommandKey.TriggerSearch, 0, pageItems(3), request(chat, "!triggersearch foo"), 0.seconds)
    store.save(chat, CommandKey.TopTwenty, 2, pageItems(2), request(chat, "!toptwenty"), 1.seconds)
    assertEquals(store.size, 1)
    assertEquals(
      store.get(chat, 1.seconds).map(e => (e.commandKey, e.page, e.pageItems.map(_.label))),
      Some((CommandKey.TopTwenty, 2, List("item 1", "item 2")))
    )
  }

  test("the entry of a chat is returned only while it is not expired") {
    val store = newStore()
    val chat  = TestIds.chatId(1L)
    store.save(chat, CommandKey.TriggerSearch, 1, pageItems(2), request(chat, "!triggersearch foo"), 0.seconds)
    assertEquals(store.get(chat, expiry).map(_.page), Some(1))
    assertEquals(store.isExpired(chat, expiry), false)
    assertEquals(store.get(chat, expiry + 1.second), None)
    assertEquals(store.isExpired(chat, expiry + 1.second), true)
  }

  test("a chat without an entry: no entry, not expired") {
    val store = newStore()
    assertEquals(store.get(TestIds.chatId(9L), 0.seconds), None)
    assertEquals(store.isExpired(TestIds.chatId(9L), 0.seconds), false)
  }

  test("the entry remembers the request: the message that asked for the list") {
    val store = newStore()
    val req   = request(TestIds.chatId(1L), "!triggersearch foo")
    store.save(TestIds.chatId(1L), CommandKey.TriggerSearch, 0, pageItems(2), req, 0.seconds)
    assertEquals(store.get(TestIds.chatId(1L), 0.seconds).map(_.request), Some(req))
  }

  test("the store keeps at most maxChats entries, the oldest dropped when full") {
    val store = newStore()
    val a     = TestIds.chatId(10L)
    val b     = TestIds.chatId(11L)
    val c     = TestIds.chatId(12L)
    val d     = TestIds.chatId(13L)
    store.save(a, CommandKey.TriggerSearch, 0, pageItems(1), request(a, "!a"), 0.seconds)
    store.save(b, CommandKey.TriggerSearch, 0, pageItems(1), request(b, "!b"), 1.second)
    store.save(c, CommandKey.TriggerSearch, 0, pageItems(1), request(c, "!c"), 2.seconds)
    assertEquals(store.size, 2)
    assertEquals(store.get(a, 3.seconds), None)
    assertEquals(store.get(b, 3.seconds).map(_.page), Some(0))
    assertEquals(store.get(c, 3.seconds).map(_.page), Some(0))
    store.save(d, CommandKey.TriggerSearch, 0, pageItems(1), request(d, "!d"), 3.seconds)
    assertEquals(store.size, 2)
    assertEquals(store.get(b, 4.seconds), None)
    assertEquals(store.get(c, 4.seconds).map(_.page), Some(0))
    assertEquals(store.get(d, 4.seconds).map(_.page), Some(0))
  }
}
