package com.benkio.chatcore.messagefiltering

object FuzzyScoring {

  val AutoSelectThreshold: Double = 0.82
  val AutoSelectMargin: Double    = 0.08

  def levScore(query: String, candidate: String): Double = {
    val nq     = FuzzyNormalization.normalize(query)
    val nc     = FuzzyNormalization.normalize(candidate)
    val maxLen = math.max(nq.length, nc.length)
    if maxLen == 0 then 1.0 else 1.0 - Levenshtein.levenshtein(nq, nc).toDouble / maxLen
  }

  def tokenScore(query: String, candidate: String): Double = {
    val wordScore = FuzzyNormalization.jaccard(FuzzyNormalization.tokens(query), FuzzyNormalization.tokens(candidate))
    if wordScore > 0.0 then wordScore
    else {
      val q = FuzzyNormalization.normalize(query).toSet
      val c = FuzzyNormalization.normalize(candidate).toSet
      FuzzyNormalization.jaccard(q, c)
    }
  }

  def score(query: String, candidate: String): Double =
    0.7 * levScore(query, candidate) + 0.3 * tokenScore(query, candidate)

  def isConfident(bestScore: Double, secondScore: Option[Double]): Boolean =
    bestScore >= AutoSelectThreshold - 1e-9 && secondScore.forall(s => bestScore - s >= AutoSelectMargin - 1e-9)
}
