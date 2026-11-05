package com.benkio.chatcore.model.show

import munit.FunSuite
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll

import java.time.LocalDate

class TitleDateParserSpec extends FunSuite with ScalaCheckSuite {

  private def parse(title: String, uploadYear: Int = 2020): Option[LocalDate] =
    TitleDateParser.parse(title, uploadYear)

  List(
    "Orlando Art LIVE - 25/11/2025 - Canale 124 DTT"                       -> LocalDate.of(2025, 11, 25),
    "Camera, 24/03/21"                                                     -> LocalDate.of(2021, 3, 24),
    "Anche la rabbia ha un cuore (2 gennaio 2008) Steve Vai"               -> LocalDate.of(2008, 1, 2),
    "RICHARD BENSON - 11 GENNAIO 2008 - 1a parte"                          -> LocalDate.of(2008, 1, 11),
    "Ottava Nota | Nomi grossi (14 Febbraio 1996) [PUNTATA INEDITA]"       -> LocalDate.of(1996, 2, 14),
    "Cocktail Micidiale 09 settembre 2005 (puntata completa)"              -> LocalDate.of(2005, 9, 9),
    "Live 23 Novembre 1999 | Presentazione"                                -> LocalDate.of(1999, 11, 23),
    "Xah Talk Show 2019-10-18. netiquette"                                 -> LocalDate.of(2019, 10, 18),
    "Xah Talk Show 2020-07-15, coding emacs lisp"                          -> LocalDate.of(2020, 7, 15),
    "xah talk show 2019-07-02. emacs"                                      -> LocalDate.of(2019, 7, 2),
    "Il 26 giugno a Roma la presentazione della lista"                     -> LocalDate.of(2020, 6, 26),
    "Il 1° maggio si festeggia"                                            -> LocalDate.of(2020, 5, 1)
  ).foreach { case (title, expected) =>
    test(s"parse '$title'") {
      assertEquals(parse(title), Some(expected))
    }
  }

  test("two digit year close to the upload year goes to the 2000s") {
    assertEquals(
      parse("Orlando Art LIVE - 29/10/26  - Canale 124 DTT", uploadYear = 2026),
      Some(LocalDate.of(2026, 10, 29))
    )
  }

  test("two digit year in the far future of the upload goes to the 1900s") {
    assertEquals(parse("registrato il 03/04/96", uploadYear = 2010), Some(LocalDate.of(1996, 4, 3)))
  }

  test("a title without date gives None") {
    assertEquals(parse("Pino Scotto su Fabrizio Miccoli"), None)
  }

  test("numbers that are not dates give None") {
    assertEquals(parse("Puntata 12 di 2020 e 5 amici"), None)
  }

  test("invalid dates give None") {
    assertEquals(parse("il 31/02/2020"), None)
    assertEquals(parse("il 31 febbraio 2020"), None)
  }

  test("29 february without year in a leap upload year is valid") {
    assertEquals(parse("il 29 febbraio"), Some(LocalDate.of(2020, 2, 29)))
  }

  test("29 february without year in a non leap upload year gives None") {
    assertEquals(parse("il 29 febbraio", uploadYear = 2021), None)
  }

  test("a dated form wins over a year-less one appearing earlier") {
    assertEquals(parse("Il 26 giugno a Roma - 11 GENNAIO 2008"), Some(LocalDate.of(2008, 1, 11)))
  }

  property("never throws and a parsed date is valid for any title") {
    forAll(Gen.asciiPrintableStr, Gen.choose(1990, 2030)) { (title, year) =>
      parse(title, year).forall(date => date.getMonthValue >= 1 && date.getDayOfMonth >= 1)
    }
  }

  property("any ISO date embedded in a title is recovered") {
    forAll(Gen.choose(1990, 2030), Gen.choose(1, 12), Gen.choose(1, 28)) { (y, m, d) =>
      val expected = LocalDate.of(y, m, d)
      parse(s"Show $expected - parte 1") == Some(expected)
    }
  }
}
