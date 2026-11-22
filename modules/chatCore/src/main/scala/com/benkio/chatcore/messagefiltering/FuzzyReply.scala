package com.benkio.chatcore.messagefiltering

import com.benkio.chatcore.model.reply.PagedItem
import com.benkio.chatcore.model.reply.PagedList
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

      case FuzzyCandidates.AutoSelect(candidate, bestScore, secondScore, ranked) =>
        val bestRbm    = candidate.replyBundleMessage.getOrElse(replyBundleMessages.head)
        val prefix     = s"No exact match for '$query'. Closest trigger selected: '${candidate.value}'."
        val others     = ranked.tail.take(FuzzyTriggerSearch.MaxSuggestions - 1).map(_._1.value)
        val replyValue =
          confidentReply(prefix = prefix, best = replyBundleTransformation(bestRbm), others = others, ttl = ttl)
        val log = Some(
          s"[FuzzyTriggerSearch] query=$query best=${candidate.value} score=$bestScore second=${secondScore.getOrElse(0.0)} decision=auto_select"
        )
        Result(replyValue, log)

      case FuzzyCandidates.Suggest(ranked) =>
        val bestScore   = ranked.headOption.map(_._2).getOrElse(0.0)
        val secondScore = ranked.lift(1).map(_._2)
        val replyValue  = PagedList.triggerSuggestions(query, ranked.map(_._1.value))
        val log         = Some(
          s"[FuzzyTriggerSearch] query=$query best=${ranked.headOption.map(_._1.value).getOrElse("")} score=$bestScore second=${secondScore.getOrElse(0.0)} decision=suggest_only"
        )
        Result(replyValue, log)
    }
  }

  /** The best bundle reply, named in the prefix, with the other candidates still available as buttons. A reply that
    * cannot offer buttons (plain text, or an adapter specific value) keeps its shape and only gets the prefix on text.
    */
  private def confidentReply(
      prefix: String,
      best: ReplyValue,
      others: List[String],
      ttl: Option[FiniteDuration]
  ): ReplyValue = best match {
    case txt: Text            => Text(value = s"$prefix\n${txt.value}", timeToLive = ttl)
    case pagedList: PagedList =>
      val items = pagedList.items ++ others.map(PagedItem.TriggerItem(_))
      pagedList.copy(
        title = s"$prefix\n${pagedList.title}",
        items = items,
        pageSize = math.max(pagedList.pageSize, items.length)
      )
    case other => other
  }

  def reply(
      query: String,
      replyBundleMessages: List[ReplyBundleMessage],
      replyBundleTransformation: ReplyBundleMessage => ReplyValue,
      ttl: Option[FiniteDuration]
  ): ReplyValue =
    build(
      query = query,
      replyBundleMessages = replyBundleMessages,
      replyBundleTransformation = replyBundleTransformation,
      ttl = ttl
    ).replyValue
}
