package com.benkio.chatcore.messagefiltering

import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text

import scala.concurrent.duration.FiniteDuration

object FuzzyReply {

  final case class Result(replyValue: ReplyValue, log: Option[String])

  def build(
      query: String,
      replyBundleMessages: List[ReplyBundleMessage],
      replyBundleTransformation: ReplyBundleMessage => ReplyValue,
      ttl: Option[FiniteDuration]
  ): Result = {
    FuzzyCandidates.decide(query, replyBundleMessages) match {
      case FuzzyCandidates.NoCandidates =>
        Result(Text(value = s"No matching trigger for $query", timeToLive = ttl), None)

      case FuzzyCandidates.AutoSelect(candidate, bestScore, secondScore) =>
        val bestRbm = candidate.replyBundleMessage.getOrElse(replyBundleMessages.head)
        val prefix = s"No exact match for '$query'. Closest trigger selected: '${candidate.value}'."
        val replyValue = replyBundleTransformation(bestRbm) match {
          case txt: Text => Text(value = s"$prefix\n${txt.value}", timeToLive = ttl)
          case other     => other
        }
        val log = Some(
          s"[FuzzyTriggerSearch] query=$query best=${candidate.value} score=$bestScore second=${secondScore.getOrElse(0.0)} decision=auto_select"
        )
        Result(replyValue, log)

      case FuzzyCandidates.Suggest(ranked) =>
        val bestScore = ranked.headOption.map(_._2).getOrElse(0.0)
        val secondScore = ranked.lift(1).map(_._2)
        val suggestions = ranked.take(5).map(_._1.value).mkString(", ")
        val replyValue = Text(
          value = s"No exact match for '$query'. Did you mean: $suggestions?",
          timeToLive = ttl
        )
        val log = Some(
          s"[FuzzyTriggerSearch] query=$query best=${ranked.headOption.map(_._1.value).getOrElse("")} score=$bestScore second=${secondScore.getOrElse(0.0)} decision=suggest_only"
        )
        Result(replyValue, log)
    }
  }

  def reply(
      query: String,
      replyBundleMessages: List[ReplyBundleMessage],
      replyBundleTransformation: ReplyBundleMessage => ReplyValue,
      ttl: Option[FiniteDuration]
  ): ReplyValue =
    build(query, replyBundleMessages, replyBundleTransformation, ttl).replyValue
}
