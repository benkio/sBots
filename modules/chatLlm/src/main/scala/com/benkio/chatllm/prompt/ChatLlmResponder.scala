package com.benkio.chatllm.prompt

import cats.effect.syntax.monadCancel.*
import cats.effect.syntax.spawn.*
import cats.effect.Clock
import cats.effect.Concurrent
import cats.syntax.all.*
import cats.Applicative
import cats.MonadThrow
import com.benkio.chatcore.llm.LlmResponder
import com.benkio.chatcore.model.reply.Reply
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatllm.llm.Completion
import com.benkio.chatllm.llm.CompletionStatus
import com.benkio.chatllm.llm.LlmClientApi
import com.benkio.chatllm.observability.LlmObservation
import com.benkio.chatllm.observability.LlmObserver
import com.benkio.chatllm.observability.LlmOutcome
import com.benkio.chatllm.LlmContext
import log.effect.LogWriter

/** #1491: the `LlmResponder` of a bot. It offers the model the persona, the catalogue entries of the candidate replies
  * the engine matched and the chat context, then turns the validated decision into a normal `Reply`:
  *   - `text`: a text reply to the message;
  *   - `reuse`: the reply of the chosen candidate, as is, so it goes through the usual reply path;
  *   - `silent`, or anything malformed, unknown, unsafe or failed: no reply.
  */
final class ChatLlmResponder[F[_]: Concurrent: Clock](
    client: LlmClientApi[F],
    context: LlmContext[F],
    builder: PromptBuilder,
    botId: SBotId,
    persona: String,
    catalogue: List[CatalogueEntry],
    observer: Option[LlmObserver[F]] = None
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
      window <- context.prompt(msg.chatId, botId)
      prompt = builder.build(persona, entries, window, msg)
      started <- Clock[F].monotonic
      // A cancelled call (the engine's own timeout, a shutdown) is recorded too, exactly once: the hook only covers the
      // client call, so the success and error paths below never run for it. The cancellation itself is preserved, and
      // it does not wait for the recording: the observation (which can write to the trace table) runs in its own fiber,
      // so a slow database cannot delay the timeout recovery and the release of the chat claim in the engine.
      attempted <- client
        .complete(prompt)
        .attempt
        .onCancel(
          Clock[F].monotonic.flatMap(now =>
            observe(
              msg = msg,
              prompt = prompt,
              latencyMs = (now - started).toMillis,
              outcome = LlmOutcome.Cancelled
            ).start.void
          )
        )
      finished <- Clock[F].monotonic
      latency = (finished - started).toMillis
      reply <- attempted match {
        case Left(error) =>
          // The failure is recorded (class only: the message may hold chat content), then handled as before below.
          observe(
            msg = msg,
            prompt = prompt,
            latencyMs = latency,
            outcome = LlmOutcome.ClientFailed(error.getClass.getSimpleName)
          ) *>
            MonadThrow[F].raiseError[Option[Reply]](error)
        case Right(completion) =>
          answer(msg = msg, prompt = prompt, latencyMs = latency, completion = completion, offered = offered)
      }
    } yield reply).handleErrorWith { error =>
      log.error(s"[ChatLlmResponder] failed: ${error.getMessage}").as(none[Reply])
    }
  }

  private def answer(
      msg: Message,
      prompt: String,
      latencyMs: Long,
      completion: Completion,
      offered: Map[String, ReplyBundleMessage]
  ): F[Option[Reply]] = {
    val decision = completion.reply
      .filter(_ => completion.status == CompletionStatus.Answered)
      .fold[LlmDecision](LlmDecision.Silent)(LlmDecision.from(_, builder.maxReplyChars))
    val reuseOffered = decision match {
      case LlmDecision.Reuse(id) => offered.contains(id)
      case _                     => false
    }
    log
      .warn(
        s"[ChatLlmResponder] no answer for ${msg.chatId}: ${completion.status}, attempts " +
          completion.attempts.map(a => s"${a.provider}=${a.result.fold("none")(_.toString)}").mkString("[", ", ", "]")
      )
      .whenA(completion.status != CompletionStatus.Answered && completion.status != CompletionStatus.Silent) *>
      observe(
        msg = msg,
        prompt = prompt,
        latencyMs = latencyMs,
        outcome = LlmOutcome.Completed(completion, decision, reuseOffered)
      ).as(
        toReply(decision, offered)
      )
  }

  private def observe(msg: Message, prompt: String, latencyMs: Long, outcome: LlmOutcome): F[Unit] =
    observer.traverse_(
      _.observe(
        LlmObservation(
          chatId = msg.chatId,
          botId = botId,
          promptVersion = builder.version,
          prompt = prompt,
          latencyMs = latencyMs,
          outcome = outcome
        )
      )
    )

  override def remember(msg: Message)(using Applicative[F]): F[Unit] =
    context.remember(msg, botId).void.handleErrorWith(rememberFailed("message", msg))

  /** A text reply is remembered as the text sent; a reused reply by the summary of its catalogue entry. */
  override def rememberAnswer(msg: Message, reply: Reply, candidates: List[ReplyBundleMessage])(using
      Applicative[F]
  ): F[Unit] = {
    // a reused reply is told apart first: it can be a text reply too, and then it is the catalogue summary that counts
    val reused = candidates
      .find(_.reply == reply)
      .flatMap(ReplyCatalogue.reusableId)
      .flatMap(summaries.get)
      .map(_.summary)
    val content = reused.orElse(reply match {
      case TextReply(texts, _) => texts.headOption.map(_.value)
      case _                   => None
    })
    content.traverse_(text => context.rememberAssistant(msg.chatId, botId, text).void)
  }.handleErrorWith(rememberFailed("answer", msg))

  private def rememberFailed(what: String, msg: Message)(error: Throwable): F[Unit] =
    log.warn(s"[ChatLlmResponder] could not remember the $what of ${msg.chatId}: ${error.getMessage}")

  private def toReply(decision: LlmDecision, offered: Map[String, ReplyBundleMessage]): Option[Reply] =
    decision match {
      case LlmDecision.Text(text) => Some(TextReply.fromList(text)(replyToMessage = true))
      case LlmDecision.Reuse(id)  => offered.get(id).map(_.reply)
      case LlmDecision.Silent     => None
    }
}
