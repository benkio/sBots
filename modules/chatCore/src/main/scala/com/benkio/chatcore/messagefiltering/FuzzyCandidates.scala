package com.benkio.chatcore.messagefiltering

import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.RegexTextTriggerValue
import com.benkio.chatcore.model.StringTextTriggerValue
import com.benkio.chatcore.model.TextTrigger

object FuzzyCandidates {

  final case class Candidate(
      value: String,
      normalized: String = "",
      priority: Int = 0,
      replyBundleMessage: Option[ReplyBundleMessage] = None
  )

  final case class Result(
      best: Option[String],
      bestScore: Double,
      second: Option[Double],
      suggestions: List[String],
      scored: List[(String, Double)] = Nil
  ) {
    def bestCandidate: Option[String]                   = best
    def bestCandidateAndScore: Option[(String, Double)] = best.map(b => b -> bestScore)
    def secondScore: Option[Double]                     = second
    def isConfident: Boolean                            = FuzzyScoring.isConfident(bestScore, second)
  }

  def candidates(replyBundleMessages: List[ReplyBundleMessage]): List[Candidate] = {
    val raw = replyBundleMessages.flatMap { rbm =>
      rbm.trigger match {
        case TextTrigger(triggers*) =>
          val strings = triggers.collect { case s: StringTextTriggerValue => s.trigger }
          val regexes = triggers.collect { case r: RegexTextTriggerValue => r.trigger.toString }
          strings.map(v =>
            Candidate(
              value = v,
              normalized = FuzzyNormalization.normalize(v),
              priority = 0,
              replyBundleMessage = Some(rbm)
            )
          ) ++
            regexes.map(v =>
              Candidate(
                value = v,
                normalized = FuzzyNormalization.normalize(v),
                priority = 1,
                replyBundleMessage = Some(rbm)
              )
            )
        case _ => Nil
      }
    }
    val indexed = raw.zipWithIndex
    indexed
      .groupBy(_._1.normalized)
      .map { case (_, group) =>
        group.minBy { case (c, idx) => (c.priority, idx) }._1
      }
      .toList
  }

  def rank(query: String, candidates: Seq[String]): List[(String, Double)] = {
    val deduped = candidates
      .map(c => c -> FuzzyNormalization.normalize(c))
      .groupBy(_._2)
      .map(_._2.head._1)
      .toSeq
    deduped
      .map(c => c -> FuzzyScoring.score(query, c))
      .sortBy { case (c, s) => (-s, 0, c) }
      .toList
  }

  def rankCandidates(query: String, candidates: Seq[Candidate]): List[(Candidate, Double)] = {
    val indexed = candidates.zipWithIndex
    val deduped = indexed
      .groupBy(_._1.normalized)
      .map { case (_, group) =>
        group.minBy { case (c, idx) => (c.priority, idx) }._1
      }
      .toSeq
    deduped
      .map(c => c -> FuzzyScoring.score(query, c.value))
      .sortBy { case (c, s) => (-s, c.priority, c.value) }
      .toList
  }

  sealed trait Decision
  case object NoCandidates                                                                 extends Decision
  final case class AutoSelect(candidate: Candidate, score: Double, second: Option[Double]) extends Decision
  final case class Suggest(ranked: List[(Candidate, Double)])                              extends Decision

  def decide(query: String, replyBundleMessages: List[com.benkio.chatcore.model.reply.ReplyBundleMessage]): Decision = {
    val cands = candidates(replyBundleMessages)
    if cands.isEmpty then NoCandidates
    else {
      val ranked    = rankCandidates(query, cands)
      val best      = ranked.headOption
      val second    = ranked.lift(1).map(_._2)
      val bestScore = best.map(_._2).getOrElse(0.0)
      if FuzzyScoring.isConfident(bestScore, second) then AutoSelect(best.get._1, bestScore, second)
      else Suggest(ranked)
    }
  }

  def search(query: String, candidates: Seq[String]): Result = {
    val ranked      = rank(query, candidates)
    val best        = ranked.headOption
    val second      = ranked.lift(1).map(_._2)
    val suggestions = ranked.take(5).map(_._1)
    Result(
      best = best.map(_._1),
      bestScore = best.map(_._2).getOrElse(0.0),
      second = second,
      suggestions = suggestions,
      scored = ranked
    )
  }
}
