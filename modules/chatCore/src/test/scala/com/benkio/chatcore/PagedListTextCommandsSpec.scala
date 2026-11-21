package com.benkio.chatcore

import com.benkio.chatcore.model.reply.PagedItem
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo.SBotName
import com.benkio.chatcore.TestIds
import munit.FunSuite

import scala.concurrent.duration.*

/** The decisions of `!next`, `!prev` and `!pick` on the paged list remembered for the chat (see [[PagedListTextStore]]
  * and [[PagedListTextCommands]]).
  */
class PagedListTextCommandsSpec extends FunSuite {

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

  private def storeWith(entry: PagedListTextStore.Entry): PagedListTextStore.Store = {
    val store = new PagedListTextStore.Store(10.minutes, 100)
    store.save(entry.request.chatId, entry.commandKey, entry.page, entry.pageItems, entry.request, entry.savedAt)
    store
  }

  private def decide(
      msg: Message,
      store: PagedListTextStore.Store,
      now: FiniteDuration = 0.seconds
  ): Option[PagedListTextCommands.Action] =
    PagedListTextCommands.decide(msg = msg, syntax = syntax, botName = botName, store = store, now = now)

  test("!next reruns the command of the entry at the next page") {
    val e = entry(page = 1, items = 3)
    assertEquals(decide(msg = message("!next"), store = storeWith(e)), Some(PagedListTextCommands.Action.Rerun(e, 2)))
  }

  test("!prev reruns the command of the entry at the previous page, staying at the first page") {
    assertEquals(
      decide(msg = message("!prev"), store = storeWith(entry(page = 1, items = 3))),
      Some(PagedListTextCommands.Action.Rerun(entry(page = 1, items = 3), 0))
    )
    assertEquals(
      decide(msg = message("!prev"), store = storeWith(entry(page = 0, items = 3))),
      Some(PagedListTextCommands.Action.Rerun(entry(page = 0, items = 3), 0))
    )
  }

  test("!pick n picks the item n of the page") {
    val e = entry(page = 0, items = 3)
    assertEquals(decide(msg = message("!pick 1"), store = storeWith(e)), Some(PagedListTextCommands.Action.Pick(e, 1)))
    assertEquals(decide(msg = message("!pick 3"), store = storeWith(e)), Some(PagedListTextCommands.Action.Pick(e, 3)))
  }

  test("!pick with the number out of the page: the clear answer") {
    val e = entry(page = 0, items = 2)
    assertEquals(
      decide(msg = message("!pick 0"), store = storeWith(e)),
      Some(PagedListTextCommands.Action.Answer(PagedListTextCommands.pickOutOfPageAnswer(0)))
    )
    assertEquals(
      decide(msg = message("!pick 3"), store = storeWith(e)),
      Some(PagedListTextCommands.Action.Answer(PagedListTextCommands.pickOutOfPageAnswer(3)))
    )
  }

  test("!pick without a number or with a non number: the clear answer") {
    val e = entry(page = 0, items = 2)
    assertEquals(
      decide(msg = message("!pick"), store = storeWith(e)),
      Some(PagedListTextCommands.Action.Answer(PagedListTextCommands.pickMissingNumberAnswer))
    )
    assertEquals(
      decide(msg = message("!pick x"), store = storeWith(e)),
      Some(PagedListTextCommands.Action.Answer(PagedListTextCommands.pickMissingNumberAnswer))
    )
  }

  test("no remembered list: the clear answer of the chat without any list") {
    val store = new PagedListTextStore.Store(10.minutes, 100)
    assertEquals(
      decide(msg = message("!next"), store = store),
      Some(PagedListTextCommands.Action.Answer(PagedListTextCommands.noListAnswer))
    )
    assertEquals(
      decide(msg = message("!prev"), store = store),
      Some(PagedListTextCommands.Action.Answer(PagedListTextCommands.noListAnswer))
    )
    assertEquals(
      decide(msg = message("!pick 1"), store = store),
      Some(PagedListTextCommands.Action.Answer(PagedListTextCommands.noListAnswer))
    )
  }

  test("an expired remembered list: the clear answer, still valid before the expiry") {
    val e     = entry(page = 0, items = 2, savedAt = 0.seconds)
    val store = storeWith(e)
    assertEquals(
      decide(msg = message("!next"), store = store, now = 10.minutes),
      Some(PagedListTextCommands.Action.Rerun(e, 1))
    )
    assertEquals(
      decide(msg = message("!next"), store = store, now = 11.minutes),
      Some(PagedListTextCommands.Action.Answer(PagedListTextCommands.expiredAnswer))
    )
  }

  test("a text that is not a text command: None") {
    val store = storeWith(entry(page = 0, items = 2))
    assertEquals(decide(msg = message("hello"), store = store), None)
    assertEquals(decide(msg = message("next"), store = store), None) // without the prefix of the platform
    assertEquals(decide(msg = message("!toptwenty"), store = store), None)
  }
}
