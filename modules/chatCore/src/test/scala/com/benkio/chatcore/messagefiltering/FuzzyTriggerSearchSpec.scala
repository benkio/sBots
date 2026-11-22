package com.benkio.chatcore.messagefiltering

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

  test("candidates extract string triggers and skip non-text") {
    // just smoke: no crash
    val bundles = Nil
    assertEquals(FuzzyTriggerSearch.candidates(bundles), Nil)
  }
}