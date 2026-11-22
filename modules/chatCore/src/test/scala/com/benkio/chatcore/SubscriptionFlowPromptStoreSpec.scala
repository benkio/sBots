package com.benkio.chatcore

import cats.effect.IO
import com.benkio.chatcore.model.*
import com.benkio.chatcore.TestIds
import munit.CatsEffectSuite

import scala.concurrent.duration.*

class SubscriptionFlowPromptStoreSpec extends CatsEffectSuite {

  private def key(chat: Long, requester: String): SubscriptionFlowKey =
    SubscriptionFlowKey(TestIds.chatId(chat), SBotInfo.SBotId("bot"), requester)

  test("the same message id in different chats addresses both prompts") {
    val store = SubscriptionFlowPromptStore.create[IO](10.minutes, 10)
    val keyA  = key(1L, "alice")
    val keyB  = key(2L, "bob")
    for {
      _   <- store.put(keyA, "42", "alice")
      _   <- store.put(keyB, "42", "bob")
      byA <- store.byMessageId(keyA.chatId, "42")
      byB <- store.byMessageId(keyB.chatId, "42")
      kA  <- store.byKey(keyA)
      kB  <- store.byKey(keyB)
    } yield {
      assertEquals(byA.map(_.key), Some(keyA))
      assertEquals(byB.map(_.key), Some(keyB))
      assertEquals(kA.map(_.owner), Some("alice"))
      assertEquals(kB.map(_.owner), Some("bob"))
    }
  }

  test("removing one chat's prompt leaves another chat's same-id prompt addressable") {
    val store = SubscriptionFlowPromptStore.create[IO](10.minutes, 10)
    val keyA  = key(1L, "alice")
    val keyB  = key(2L, "bob")
    for {
      _      <- store.put(keyA, "42", "alice")
      _      <- store.put(keyB, "42", "bob")
      _      <- store.remove(keyA)
      gone   <- store.byMessageId(keyA.chatId, "42")
      kept   <- store.byMessageId(keyB.chatId, "42")
      byKeyA <- store.byKey(keyA)
    } yield {
      assertEquals(gone, None)
      assertEquals(kept.map(_.key), Some(keyB))
      assertEquals(byKeyA, None)
    }
  }

  test("touch refreshes its prompt without touching another chat's same-id prompt") {
    val store = SubscriptionFlowPromptStore.create[IO](10.minutes, 10)
    val keyA  = key(1L, "alice")
    val keyB  = key(2L, "bob")
    for {
      _      <- store.put(keyA, "42", "alice")
      _      <- store.put(keyB, "42", "bob")
      beforeA <- store.byMessageId(keyA.chatId, "42")
      beforeB <- store.byMessageId(keyB.chatId, "42")
      _      <- store.touch(keyA)
      afterA <- store.byMessageId(keyA.chatId, "42")
      afterB <- store.byMessageId(keyB.chatId, "42")
    } yield {
      assertEquals(afterA.map(_.key), Some(keyA))
      assert(beforeA.forall(b => afterA.exists(_.savedAt >= b.savedAt)))
      assertEquals(afterB.map(_.savedAt), beforeB.map(_.savedAt))
    }
  }

  test("eviction drops only the evicted chat's reverse entry") {
    val store = SubscriptionFlowPromptStore.create[IO](10.minutes, 1)
    val keyA  = key(1L, "alice")
    val keyB  = key(2L, "bob")
    for {
      _   <- store.put(keyA, "42", "alice")
      _   <- IO.sleep(2.millis) // the first handle must be the oldest when the bound evicts one
      _   <- store.put(keyB, "42", "bob")
      byA <- store.byKey(keyA)
      byB <- store.byKey(keyB)
      a   <- store.byMessageId(keyA.chatId, "42")
      b   <- store.byMessageId(keyB.chatId, "42")
    } yield {
      assertEquals(byA, None)
      assertEquals(byB.map(_.key), Some(keyB))
      assertEquals(a, None)
      assertEquals(b.map(_.key), Some(keyB))
    }
  }
}
