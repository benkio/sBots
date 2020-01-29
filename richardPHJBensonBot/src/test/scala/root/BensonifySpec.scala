package root

import org.scalatest._

class BensonifySpec extends WordSpec with Matchers {

  "Bensonify" should {
    "convert propertly" when {
      "A bunch of special cases are provided" in {

        val cases : Map[String, String] = Map(
          "capito" -> "gabido",
          "spaventare" -> "sbavendare",
          "arrivato" -> "arivado",
          "ultimi" -> "uldimi"
        )

        cases foreach {
          case (case1, expected) =>
            Bensonify.compute(case1) shouldEqual expected
        }

      }
    }
  }
}
