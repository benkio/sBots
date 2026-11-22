package com.benkio.chatcore.messagefiltering

import java.text.Normalizer
import java.util.Locale

object FuzzyNormalization {

  def normalize(input: String): String = {
    val nfd          = Normalizer.normalize(Option(input).getOrElse(""), Normalizer.Form.NFD)
    val noDiacritics = nfd.replaceAll("\\p{M}", "")
    val lower        = noDiacritics.toLowerCase(Locale.ROOT)
    val noPunct      = lower.replaceAll("[^\\p{L}\\p{N}\\s]", " ")
    noPunct.trim.replaceAll("\\s+", " ")
  }

  def tokens(input: String): Set[String] = {
    val n = normalize(input)
    if n.isEmpty then Set.empty else n.split(" ").filter(_.nonEmpty).toSet
  }

  def jaccard[A](a: Set[A], b: Set[A]): Double =
    if a.isEmpty && b.isEmpty then 1.0
    else {
      val inter = a.intersect(b).size.toDouble
      val union = a.union(b).size.toDouble
      if union == 0.0 then 0.0 else inter / union
    }
}
