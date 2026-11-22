package com.benkio.chatcore.messagefiltering

object Levenshtein {

  def levenshtein(a: String, b: String): Int = {
    val s = Option(a).getOrElse("")
    val t = Option(b).getOrElse("")
    if s.isEmpty then t.length
    else if t.isEmpty then s.length
    else {
      val prev = Array.tabulate(t.length + 1)(identity)
      val curr = new Array[Int](t.length + 1)
      var i    = 0
      while i < s.length do {
        curr(0) = i + 1
        var j = 0
        while j < t.length do {
          val cost = if s.charAt(i) == t.charAt(j) then 0 else 1
          curr(j + 1) = math.min(math.min(curr(j) + 1, prev(j + 1) + 1), prev(j) + cost)
          j += 1
        }
        var k = 0
        while k <= t.length do { prev(k) = curr(k); k += 1 }
        i += 1
      }
      prev(t.length)
    }
  }
}
