package root

import org.scalatest._
import org.scalatest.wordspec.AnyWordSpec
import matchers.should._

class BensonifySpec extends AnyWordSpec with Matchers {

  "Bensonify" should {
    "convert propertly" when {
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
      "A bunch of special cases are provided" in {
        (cases ++ upperCases).foreach { case (case1, expected) =>
          Bensonify.compute(case1) shouldEqual expected
        }
      }
    }
  }
}
