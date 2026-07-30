package com.benkio.RichardPHJBensonBot

import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*

class BensonifySpec extends ScalaCheckSuite {

  val cases: Map[String, String] = Map(
    "capito"     -> "gabido",
    "spaventare" -> "sbavendare",
    "arrivato"   -> "arivado",
    "ultimi"     -> "uldimi"
  )
  val upperCases: Map[String, String] = Map(
    "CAPITO"     -> "GABIDO",
    "SPAVENTARE" -> "SBAVENDARE",
    "ARRIVATO"   -> "ARIVADO",
    "ULTIMI"     -> "ULDIMI"
  )

  test("Bensonify should convert properly when special cases are provided") {
    (cases ++ upperCases).foreach { case (case1, expected) =>
      assertEquals(Bensonify.compute(case1), expected)
    }
  }

  property("compute equals folding Bensonify.patterns (lower and upper)") {
    forAll(Gen.alphaStr) { (input: String) =>
      val expected =
        (Bensonify.patterns.map { case (k, v) => (k.toUpperCase, v.toUpperCase) } ++ Bensonify.patterns)
          .foldLeft(input) { case (acc, (patternKey, patternValue)) =>
            acc.replace(patternKey, patternValue)
          }
      assertEquals(Bensonify.compute(input), expected)
    }
  }
}
