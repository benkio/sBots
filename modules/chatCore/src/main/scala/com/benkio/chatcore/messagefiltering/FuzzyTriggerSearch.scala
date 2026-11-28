package com.benkio.chatcore.messagefiltering

object FuzzyTriggerSearch {

  type Candidate = FuzzyCandidates.Candidate
  val Candidate = FuzzyCandidates.Candidate

  type Result = FuzzyCandidates.Result
  val Result = FuzzyCandidates.Result

  type Decision = FuzzyCandidates.Decision
  val NoCandidates = FuzzyCandidates.NoCandidates
  val AutoSelect   = FuzzyCandidates.AutoSelect
  val Suggest      = FuzzyCandidates.Suggest

  val MaxSuggestions: Int = 5

  def normalize(input: String): String = FuzzyNormalization.normalize(input)

  def levenshtein(a: String, b: String): Int = Levenshtein.levenshtein(a, b)

  def tokens(input: String): Set[String] = FuzzyNormalization.tokens(input)

  def jaccard[A](a: Set[A], b: Set[A]): Double = FuzzyNormalization.jaccard(a, b)

  def levScore(query: String, candidate: String): Double = FuzzyScoring.levScore(query, candidate)

  def tokenScore(query: String, candidate: String): Double = FuzzyScoring.tokenScore(query, candidate)

  def score(query: String, candidate: String): Double = FuzzyScoring.score(query, candidate)

  def isConfident(bestScore: Double, secondScore: Option[Double]): Boolean =
    FuzzyScoring.isConfident(bestScore, secondScore)

  val AutoSelectThreshold: Double = FuzzyScoring.AutoSelectThreshold
  val AutoSelectMargin: Double    = FuzzyScoring.AutoSelectMargin

  def rank(query: String, candidates: Seq[String]): List[(String, Double)] =
    FuzzyCandidates.rank(query, candidates)

  def rankCandidates(query: String, candidates: Seq[Candidate]): List[(Candidate, Double)] =
    FuzzyCandidates.rankCandidates(query, candidates)

  def search(query: String, candidates: Seq[String]): Result =
    FuzzyCandidates.search(query, candidates)

  def decide(
      query: String,
      replyBundleMessages: List[com.benkio.chatcore.model.reply.ReplyBundleMessage]
  ): Decision =
    FuzzyCandidates.decide(query, replyBundleMessages)

  def reply(
      query: String,
      replyBundleMessages: List[com.benkio.chatcore.model.reply.ReplyBundleMessage],
      replyBundleTransformation: com.benkio.chatcore.model.reply.ReplyBundleMessage => com.benkio.chatcore.model.reply.ReplyValue,
      ttl: Option[scala.concurrent.duration.FiniteDuration]
  ): com.benkio.chatcore.model.reply.ReplyValue =
    FuzzyReply.reply(query, replyBundleMessages, replyBundleTransformation, ttl)

  def replyWithLog(
      query: String,
      replyBundleMessages: List[com.benkio.chatcore.model.reply.ReplyBundleMessage],
      replyBundleTransformation: com.benkio.chatcore.model.reply.ReplyBundleMessage => com.benkio.chatcore.model.reply.ReplyValue,
      ttl: Option[scala.concurrent.duration.FiniteDuration]
  ): FuzzyReply.Result =
    FuzzyReply.build(query, replyBundleMessages, replyBundleTransformation, ttl)

  def candidates(replyBundleMessages: List[com.benkio.chatcore.model.reply.ReplyBundleMessage]): List[Candidate] =
    FuzzyCandidates.candidates(replyBundleMessages)
}
