package com.benkio.chatcore

import cats.effect.IO
import cats.syntax.all.*
import com.benkio.chatcore.model.*
import com.benkio.chatcore.TestIds
import munit.CatsEffectSuite

import scala.concurrent.duration.*

class SubscriptionDraftStoreSpec extends CatsEffectSuite {

  private def draft(index: Int, savedAt: FiniteDuration): SubscriptionDraft =
    SubscriptionDraft(
      key = SubscriptionFlowKey(TestIds.chatId(index.toLong), SBotInfo.SBotId("eng"), s"user$index"),
      step = SubscriptionDraftStep.Type,
      subscriptionType = None,
      schedule = None,
      target = None,
      savedAt = savedAt
    )

  test("start inserts a draft, declines a full store and still allows a same-key restart") {
    val store = SubscriptionDraftStore.create[IO](10.minutes, 1)
    for {
      now <- IO.realTime
      a = draft(0, now)
      b = draft(1, now)
      c = draft(0, now + 1.second)
      r1     <- store.start(a)
      r2     <- store.start(b)
      r3     <- store.start(c)
      stored <- store.get(a.key)
      size   <- store.size
    } yield {
      assertEquals(r1, SubscriptionDraftStartResult.Started)
      assertEquals(r2, SubscriptionDraftStartResult.Full)
      assertEquals(r3, SubscriptionDraftStartResult.Started)
      assertEquals(stored, Some(c))
      assertEquals(size, 1)
    }
  }

  test("put updates a live draft, inserts while there is room and refuses a new key when full") {
    val store = SubscriptionDraftStore.create[IO](10.minutes, 2)
    for {
      now <- IO.realTime
      a = draft(0, now)
      b = draft(1, now)
      c = draft(2, now)
      _      <- store.start(a)
      update <- store.put(a.copy(step = SubscriptionDraftStep.Confirm))
      insert <- store.put(b)
      refuse <- store.put(c)
      afterA <- store.get(a.key)
      afterB <- store.get(b.key)
      afterC <- store.get(c.key)
      size   <- store.size
    } yield {
      assertEquals(update, SubscriptionDraftPutResult.Saved)
      assertEquals(insert, SubscriptionDraftPutResult.Saved)
      assertEquals(refuse, SubscriptionDraftPutResult.Full)
      assertEquals(afterA.map(_.step), Some(SubscriptionDraftStep.Confirm))
      assertEquals(afterB, Some(b))
      assertEquals(afterC, None)
      assertEquals(size, 2)
    }
  }

  test("put refuses to revive an expired draft when the live store is full") {
    val store = SubscriptionDraftStore.create[IO](10.minutes, 1)
    for {
      now <- IO.realTime
      stale = draft(0, now - 1.hour)
      fresh = draft(1, now)
      _       <- store.start(stale) // stored already expired
      _       <- store.start(fresh) // prunes stale and fills the only slot
      revived <- store.put(draft(0, now))
      after   <- store.get(stale.key)
      size    <- store.size
    } yield {
      assertEquals(revived, SubscriptionDraftPutResult.Full)
      assertEquals(after, None)
      assertEquals(size, 1)
    }
  }

  test("concurrent starts report Started only for the draft the store committed") {
    val store = SubscriptionDraftStore.create[IO](10.minutes, 500)
    for {
      now <- IO.realTime
      // a near-full map makes each update callback long enough for CAS retries to overlap
      _       <- List.range(0, 499).traverse_(i => store.start(draft(i, now)))
      results <- List.range(500, 700).map(i => store.start(draft(i, now))).parSequence
      size    <- store.size
    } yield {
      assertEquals(results.count(_ == SubscriptionDraftStartResult.Started), 1)
      assertEquals(size, 500)
    }
  }
}
