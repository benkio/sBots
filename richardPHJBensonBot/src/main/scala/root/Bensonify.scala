package root

object Bensonify {

  val patterns: Map[String, String] = Map(
    "t"  -> "d",
    "rr" -> "r",
    "c"  -> "g",
    "b"  -> "bb",
    "p"  -> "b"
  )

  def compute(input: String): String =
    (patterns.map { case (k, v) => (k.toUpperCase, v.toUpperCase) } ++ patterns)
      .foldLeft(input) {
        case (acc, (patternKey, patternValue)) =>
          acc.replace(patternKey, patternValue)
      }

}
