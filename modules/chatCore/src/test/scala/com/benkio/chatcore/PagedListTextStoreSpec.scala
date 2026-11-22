package com.benkio.chatcore

import cats.effect.IO
import com.benkio.chatcore.model.reply.PagedItem
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.TestIds
import munit.CatsEffectSuite

import scala.concurrent.duration.*

class PagedListTextStoreSpec extends CatsEffectSuite {

  private val expiry = 10.minutes

  private def newStore(): PagedListTextStore.Store[IO] = PagedListTextStore.inMemory[IO](expiry, maxChats = 2)

  private def request(chatId: ChatId, text: String): Message =
    Message(None, 0L, chatId, "private", text = Some(text))

  private def pageItems(count: Int): List[PagedItem] =
    (1 to count).toList.map(n => PagedItem.ValueItem(Text(s"item $n")))

  test("a chat has one entry at most: a new entry replaces the old one") {
    val store = newStore()
    val chat  = TestIds.chatId(1L)
    for {
      _ <- store.save(chat, CommandKey.TriggerSearch, 0, pageItems(3), request(chat, "!triggersearch foo"), 0.seconds)
      _ <- store.save(chat, CommandKey.TopTwenty, 2, pageItems(2), request(chat, "!toptwenty"), 1.seconds)
      size <- store.size
      got  <- store.get(chat, 1.seconds)
    } yield {
      assertEquals(size, 1)
      assertEquals(
        got.map(e => (e.commandKey, e.page, e.pageItems.map(_.label))),
        Some((CommandKey.TopTwenty, 2, List("item 1", "item 2")))
      )
    }
  }

  test("the entry of a chat is returned only while it is not expired") {
    val store = newStore()
    val chat  = TestIds.chatId(1L)
    for {
      _ <- store.save(chat, CommandKey.TriggerSearch, 1, pageItems(2), request(chat, "!triggersearch foo"), 0.seconds)
      atExpiry <- store.get(chat, expiry)
      notExp   <- store.isExpired(chat, expiry)
      after    <- store.get(chat, expiry + 1.second)
      isExp    <- store.isExpired(chat, expiry + 1.second)
    } yield {
      assertEquals(atExpiry.map(_.page), Some(1))
      assertEquals(notExp, false)
      assertEquals(after, None)
      assertEquals(isExp, true)
    }
  }

  test("a chat without an entry: no entry, not expired") {
    val store = newStore()
    for {
      got     <- store.get(TestIds.chatId(9L), 0.seconds)
      expired <- store.isExpired(TestIds.chatId(9L), 0.seconds)
    } yield {
      assertEquals(got, None)
      assertEquals(expired, false)
    }
  }

  test("the entry remembers the request: the message that asked for the list") {
    val store = newStore()
    val req   = request(TestIds.chatId(1L), "!triggersearch foo")
    for {
      _   <- store.save(TestIds.chatId(1L), CommandKey.TriggerSearch, 0, pageItems(2), req, 0.seconds)
      got <- store.get(TestIds.chatId(1L), 0.seconds)
    } yield assertEquals(got.map(_.request), Some(req))
  }

  test("the store keeps at most maxChats entries, the oldest dropped when full") {
    val store = newStore()
    val a     = TestIds.chatId(10L)
    val b     = TestIds.chatId(11L)
    val c     = TestIds.chatId(12L)
    val d     = TestIds.chatId(13L)
    for {
      _     <- store.save(a, CommandKey.TriggerSearch, 0, pageItems(1), request(a, "!a"), 0.seconds)
      _     <- store.save(b, CommandKey.TriggerSearch, 0, pageItems(1), request(b, "!b"), 1.second)
      _     <- store.save(c, CommandKey.TriggerSearch, 0, pageItems(1), request(c, "!c"), 2.seconds)
      size1 <- store.size
      gotA  <- store.get(a, 3.seconds)
      gotB  <- store.get(b, 3.seconds)
      gotC  <- store.get(c, 3.seconds)
      _     <- store.save(d, CommandKey.TriggerSearch, 0, pageItems(1), request(d, "!d"), 3.seconds)
      size2 <- store.size
      gotB2 <- store.get(b, 4.seconds)
      gotC2 <- store.get(c, 4.seconds)
      gotD  <- store.get(d, 4.seconds)
    } yield {
      assertEquals(size1, 2)
      assertEquals(gotA, None)
      assertEquals(gotB.map(_.page), Some(0))
      assertEquals(gotC.map(_.page), Some(0))
      assertEquals(size2, 2)
      assertEquals(gotB2, None)
      assertEquals(gotC2.map(_.page), Some(0))
      assertEquals(gotD.map(_.page), Some(0))
    }
  }
}
