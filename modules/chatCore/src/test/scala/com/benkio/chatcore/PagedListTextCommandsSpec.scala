package com.benkio.chatcore

import cats.effect.IO
import com.benkio.chatcore.model.reply.PagedItem
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo.SBotName
import com.benkio.chatcore.TestIds
import munit.CatsEffectSuite

import scala.concurrent.duration.*

/** The decisions of `!next`, `!prev` and `!pick` on the paged list remembered for the chat (see [[PagedListTextStore]]
  * and [[PagedListTextCommands]]).
  */
class PagedListTextCommandsSpec extends CatsEffectSuite {

  private val syntax  = CommandSyntax(prefix = "!")
  private val botName = SBotName("EngineBot")

  private def message(text: String, chatId: ChatId = TestIds.chatId(1L)): Message =
    Message(None, 0L, chatId, "private", text = Some(text))

  private def pageItems(count: Int): List[PagedItem] =
    (1 to count).toList.map(n => PagedItem.ValueItem(Text(s"item $n")))

  private def entry(page: Int, items: Int, savedAt: FiniteDuration = 0.seconds): PagedListTextStore.Entry =
    PagedListTextStore.Entry(
      commandKey = CommandKey.TriggerSearch,
      page = page,
      pageItems = pageItems(items),
      request = message("!triggersearch foo"),
      savedAt = savedAt
    )

  private def storeWith(entry: PagedListTextStore.Entry): IO[PagedListTextStore.Store[IO]] = {
    val store = PagedListTextStore.inMemory[IO](10.minutes, 100)
    store
      .save(entry.request.chatId, entry.commandKey, entry.page, entry.pageItems, entry.request, entry.savedAt)
      .as(store)
  }

  private def decide(
      msg: Message,
      store: PagedListTextStore.Store[IO],
      now: FiniteDuration = 0.seconds
  ): IO[Option[PagedListTextCommands.Action]] =
    PagedListTextCommands.decide[IO](msg = msg, syntax = syntax, botName = botName, store = store, now = now)

  test("!next reruns the command of the entry at the next page") {
    val e = entry(page = 1, items = 3)
    storeWith(e).flatMap(store => decide(msg = message("!next"), store = store)).map { result =>
      assertEquals(result, Some(PagedListTextCommands.Action.Rerun(e, 2)))
    }
  }

  test("!prev reruns the command of the entry at the previous page, staying at the first page") {
    val e1 = entry(page = 1, items = 3)
    val e0 = entry(page = 0, items = 3)
    for {
      r1 <- storeWith(e1).flatMap(store => decide(msg = message("!prev"), store = store))
      r2 <- storeWith(e0).flatMap(store => decide(msg = message("!prev"), store = store))
    } yield {
      assertEquals(r1, Some(PagedListTextCommands.Action.Rerun(e1, 0)))
      assertEquals(r2, Some(PagedListTextCommands.Action.Rerun(e0, 0)))
    }
  }

  test("!pick n picks the item n of the page") {
    val e = entry(page = 0, items = 3)
    storeWith(e).flatMap { store =>
      for {
        r1 <- decide(msg = message("!pick 1"), store = store)
        r3 <- decide(msg = message("!pick 3"), store = store)
      } yield {
        assertEquals(r1, Some(PagedListTextCommands.Action.Pick(e, 1)))
        assertEquals(r3, Some(PagedListTextCommands.Action.Pick(e, 3)))
      }
    }
  }

  test("!pick with the number out of the page: the clear answer") {
    val e = entry(page = 0, items = 2)
    storeWith(e).flatMap { store =>
      for {
        r0 <- decide(msg = message("!pick 0"), store = store)
        r3 <- decide(msg = message("!pick 3"), store = store)
      } yield {
        assertEquals(r0, Some(PagedListTextCommands.Action.Answer(PagedListTextCommands.pickOutOfPageAnswer(0))))
        assertEquals(r3, Some(PagedListTextCommands.Action.Answer(PagedListTextCommands.pickOutOfPageAnswer(3))))
      }
    }
  }

  test("!pick without a number or with a non number: the clear answer") {
    val e = entry(page = 0, items = 2)
    storeWith(e).flatMap { store =>
      for {
        rEmpty <- decide(msg = message("!pick"), store = store)
        rX     <- decide(msg = message("!pick x"), store = store)
      } yield {
        assertEquals(rEmpty, Some(PagedListTextCommands.Action.Answer(PagedListTextCommands.pickMissingNumberAnswer)))
        assertEquals(rX, Some(PagedListTextCommands.Action.Answer(PagedListTextCommands.pickMissingNumberAnswer)))
      }
    }
  }

  test("no remembered list: the clear answer of the chat without any list") {
    val store = PagedListTextStore.inMemory[IO](10.minutes, 100)
    for {
      rNext <- decide(msg = message("!next"), store = store)
      rPrev <- decide(msg = message("!prev"), store = store)
      rPick <- decide(msg = message("!pick 1"), store = store)
    } yield {
      assertEquals(rNext, Some(PagedListTextCommands.Action.Answer(PagedListTextCommands.noListAnswer)))
      assertEquals(rPrev, Some(PagedListTextCommands.Action.Answer(PagedListTextCommands.noListAnswer)))
      assertEquals(rPick, Some(PagedListTextCommands.Action.Answer(PagedListTextCommands.noListAnswer)))
    }
  }

  test("an expired remembered list: the clear answer, still valid before the expiry") {
    val e = entry(page = 0, items = 2, savedAt = 0.seconds)
    storeWith(e).flatMap { store =>
      for {
        before <- decide(msg = message("!next"), store = store, now = 10.minutes)
        after  <- decide(msg = message("!next"), store = store, now = 11.minutes)
      } yield {
        assertEquals(before, Some(PagedListTextCommands.Action.Rerun(e, 1)))
        assertEquals(after, Some(PagedListTextCommands.Action.Answer(PagedListTextCommands.expiredAnswer)))
      }
    }
  }

  test("a text that is not a text command: None") {
    storeWith(entry(page = 0, items = 2)).flatMap { store =>
      for {
        hello <- decide(msg = message("hello"), store = store)
        next  <- decide(msg = message("next"), store = store)
        top   <- decide(msg = message("!toptwenty"), store = store)
      } yield {
        assertEquals(hello, None)
        assertEquals(next, None)
        assertEquals(top, None)
      }
    }
  }
}
