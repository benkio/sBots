package com.benkio.chatllm.prompt

import cats.syntax.all.*
import cats.MonadThrow
import com.benkio.chatcore.llm.LlmResponder
import com.benkio.chatcore.model.reply.Reply
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatllm.llm.CompletionStatus
import com.benkio.chatllm.llm.LlmClientApi
import com.benkio.chatllm.LlmContext
import log.effect.LogWriter

/** #1491: the `LlmResponder` of a bot. It offers the model the persona, the catalogue entries of the candidate replies
  * the engine matched and the chat context, then turns the validated decision into a normal `Reply`:
  *   - `text`: a text reply to the message;
  *   - `reuse`: the reply of the chosen candidate, as is, so it goes through the usual reply path;
  *   - `silent`, or anything malformed, unknown, unsafe or failed: no reply.
  */
final class ChatLlmResponder[F[_]: MonadThrow](
    client: LlmClientApi[F],
    context: LlmContext[F],
    builder: PromptBuilder,
    botId: SBotId,
    persona: String,
    catalogue: List[CatalogueEntry]
)(using log: LogWriter[F])
    extends LlmResponder[F] {

  private val summaries: Map[String, CatalogueEntry] = catalogue.map(e => e.id -> e).toMap

  override def respond(msg: Message, candidates: List[ReplyBundleMessage]): F[Option[Reply]] = {
    // Only candidates the catalogue describes can be offered, and only when their id is unambiguous: a bundle without
    // a text trigger, or sharing its id with another candidate, could make a reuse send the wrong reply.
    val withIds = candidates.flatMap(c => ReplyCatalogue.reusableId(c).filter(summaries.contains).map(_ -> c))
    val offered: Map[String, ReplyBundleMessage] =
      withIds.groupBy(_._1).collect { case (id, List((_, bundle))) => id -> bundle }
    val entries = offered.keys.toList.sorted.flatMap(summaries.get)

    (for {
      window     <- context.prompt(msg.chatId, botId)
      completion <- client.complete(builder.build(persona, entries, window, msg))
      _          <- log
        .warn(
          s"[ChatLlmResponder] no answer for ${msg.chatId}: ${completion.status}, attempts " +
            completion.attempts.map(a => s"${a.provider}=${a.result.fold("none")(_.toString)}").mkString("[", ", ", "]")
        )
        .whenA(completion.status != CompletionStatus.Answered && completion.status != CompletionStatus.Silent)
      decision = completion.reply
        .filter(_ => completion.status == CompletionStatus.Answered)
        .fold[LlmDecision](LlmDecision.Silent)(LlmDecision.from(_, builder.maxReplyChars))
    } yield toReply(decision, offered)).handleErrorWith { error =>
      log.error(s"[ChatLlmResponder] failed: ${error.getMessage}").as(none[Reply])
    }
  }

  private def toReply(decision: LlmDecision, offered: Map[String, ReplyBundleMessage]): Option[Reply] =
    decision match {
      case LlmDecision.Text(text) => Some(TextReply.fromList(text)(replyToMessage = true))
      case LlmDecision.Reuse(id)  => offered.get(id).map(_.reply)
      case LlmDecision.Silent     => None
    }
}
