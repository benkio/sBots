package com.benkio.chatcore.messagefiltering

import com.benkio.chatcore.model.reply.mp3
import com.benkio.chatcore.model.reply.PagedItem
import com.benkio.chatcore.model.reply.PagedList
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import munit.FunSuite

class FuzzyTriggerSearchSpec extends FunSuite {

  test("normalize lowercases, strips diacritics and punctuation, collapses whitespace") {
    assertEquals(FuzzyTriggerSearch.normalize("  Frànçèsimo!!!  "), "francesimo")
    assertEquals(FuzzyTriggerSearch.normalize("A  B\tC"), "a b c")
  }

  test("ranking is deterministic") {
    val r1 = FuzzyTriggerSearch.rank("fransesimo", Seq("francesismo", "frocio", "ostia"))
    val r2 = FuzzyTriggerSearch.rank("fransesimo", Seq("francesismo", "frocio", "ostia"))
    assertEquals(r1, r2)
    assertEquals(r1.head._1, "francesismo")
  }

  test("threshold boundaries") {
    assert(FuzzyTriggerSearch.isConfident(0.82, Some(0.74)))
    assert(!FuzzyTriggerSearch.isConfident(0.819999, Some(0.74)))
    assert(!FuzzyTriggerSearch.isConfident(0.82, Some(0.741)))
    assert(FuzzyTriggerSearch.isConfident(0.82, None))
  }

  test("levenshtein") {
    assertEquals(FuzzyTriggerSearch.levenshtein("kitten", "sitting"), 3)
  }

  test("a confident typo replies with the chosen bundle and keeps the other candidates as buttons") {
    val bundles = List(
      ReplyBundleMessage.textToMedia("francesismo")(mp3"a.mp3"),
      ReplyBundleMessage.textToMedia("francofonia")(mp3"b.mp3")
    )

    val bestAsList: com.benkio.chatcore.model.reply.ReplyValue = PagedList(
      title = "found",
      items = List(PagedItem.ValueItem(com.benkio.chatcore.model.reply.Text("bundle"))),
      page = 0,
      commandKey = com.benkio.chatcore.model.CommandKey.TriggerSearch
    )
    FuzzyTriggerSearch.reply("fransesimo", bundles, _ => bestAsList, None) match {
      case pagedList: PagedList =>
        assert(pagedList.title.startsWith("No exact match for 'fransesimo'. Closest trigger selected: 'francesismo'."))
        assertEquals(pagedList.items.headOption.map(_.label), Some("bundle"))
        assert(pagedList.items.contains(PagedItem.TriggerItem("francofonia")))
        assert(!pagedList.hasNext)
      case other => fail(s"expected a PagedList, got: $other")
    }
  }

  test("an ambiguous query replies with a one-page keyboard of trigger candidates") {
    val bundles = List(
      ReplyBundleMessage.textToMedia("abd")(mp3"a.mp3"),
      ReplyBundleMessage.textToMedia("abe")(mp3"b.mp3")
    )

    FuzzyTriggerSearch.reply("abc", bundles, _ => com.benkio.chatcore.model.reply.Text("bundle"), None) match {
      case pagedList: PagedList =>
        assertEquals(pagedList.title, "No exact match for 'abc'. Did you mean:")
        assertEquals(pagedList.page, 0)
        assertEquals(pagedList.items.toSet, Set(PagedItem.TriggerItem("abd"), PagedItem.TriggerItem("abe")))
        assert(!pagedList.hasNext && !pagedList.hasPrevious)
      case other => fail(s"expected a PagedList, got: $other")
    }
  }

  test("candidates extract string triggers and skip non-text") {
    // just smoke: no crash
    val bundles = Nil
    assertEquals(FuzzyTriggerSearch.candidates(bundles), Nil)
  }
}
