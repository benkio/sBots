package root

object Bensonify {

  val patterns : Map[String, String] = Map(
    "t" -> "d",
    "rr" -> "r",
    "c" -> "g",
    "b" -> "bb",
    "p" -> "b"
  )

  def compute(input : String) : String =
    input.toList.foldLeft("")((acc, c) =>
      acc + patterns.get(c.toString).getOrElse(c)
    )

}
