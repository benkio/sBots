package com.benkio.richardphjbensonbot

import munit.FunSuite
class BensonifySpec extends FunSuite {

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

  test("Bensonify should convert propertly when A bunch of special cases are provided") {
    (cases ++ upperCases).foreach { case (case1, expected) =>
      assertEquals(Bensonify.compute(case1), expected)
    }
  }
}
