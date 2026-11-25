package com.benkio.chatllm.prompt

import cats.effect.syntax.monadCancel.*
import cats.effect.syntax.spawn.*
import cats.effect.Clock
import cats.effect.Concurrent
import cats.effect.Ref
import cats.syntax.all.*
import cats.Applicative
import cats.FlatMap
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
import com.benkio.chatllm.observability.LlmInteraction
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

  override def respond(msg: Message, candidates: List[ReplyBundleMessage]): F[Option[Reply]] =
    respondAfter(msg = msg, candidates = candidates, typing = Applicative[F].unit)

  /** The typing action, the prompt and the client call are one sequence for the cancellation record: the engine's
    * timeout can fire in any of them, and each would otherwise leave no interaction line.
    */
  override def respondAfter(msg: Message, candidates: List[ReplyBundleMessage], typing: F[Unit])(using
      FlatMap[F]
  ): F[Option[Reply]] =
    for {
      started <- Clock[F].monotonic
      phase   <- Ref.of[F, ResponderPhase](ResponderPhase.Typing)
      // A cancelled interaction (the engine's own timeout, a shutdown) is recorded exactly once, whatever the phase: the
      // success and error paths never run for it. The cancellation itself is preserved, and it does not wait for the
      // recording: the observation (which can write to the trace table) runs in its own fiber, so a slow database
      // cannot delay the timeout recovery and the release of the chat claim in the engine.
      reply <- run(msg = msg, candidates = candidates, typing = typing, phase = phase).onCancel(
        for {
          now      <- Clock[F].monotonic
          snapshot <- phase.get
          // once `Done`, the success or failure path owns the record and a cancel writes no `cancelled` one
          _ <- ResponderPhase
            .cancelled(snapshot)
            .traverse_((label, prompt) =>
              observeCancelled(
                msg = msg,
                prompt = prompt,
                phase = label,
                latencyMs = (now - started).toMillis
              ).start.void
            )
        } yield ()
      )
    } yield reply

  private def observeCancelled(msg: Message, prompt: String, phase: String, latencyMs: Long): F[Unit] =
    // the phase is a fixed label, never exception text or chat content
    LogWriter.info(s"[ChatLlmResponder] ${msg.chatId}: cancelled during $phase").handleError(_ => ()) *>
      observe(msg = msg, prompt = prompt, latencyMs = latencyMs, outcome = LlmOutcome.Cancelled)

  private def run(
      msg: Message,
      candidates: List[ReplyBundleMessage],
      typing: F[Unit],
      phase: Ref[F, ResponderPhase]
  ): F[Option[Reply]] = {
    // Only candidates the catalogue describes can be offered, and only when their id is unambiguous: a bundle without
    // a text trigger, or sharing its id with another candidate, could make a reuse send the wrong reply.
    val withIds = candidates.flatMap(c => ReplyCatalogue.reusableId(c).filter(summaries.contains).map(_ -> c))
    val offered: Map[String, ReplyBundleMessage] =
      withIds.groupBy(_._1).collect { case (id, List((_, bundle))) => id -> bundle }
    val entries = offered.keys.toList.sorted.flatMap(summaries.get)
    // The engine offers no candidates today (see the known defect in specs/llm.allium): a `reuse` decision can only
    // end as silence, and this line makes the empty catalogue visible without a debug trace.
    val candidatesLine =
      s"[ChatLlmResponder] ${msg.chatId}: ${candidates.size} candidates, ${offered.size} reusable, prompt ${builder.version}"

    (for {
      _ <- typing
      _ <- LogWriter.info(candidatesLine)
      _ <- phase.set(ResponderPhase.BuildingPrompt)
      // The prompt is built before the client call, so a failure here has no interaction line: log the phase and the
      // exception class (the message may hold chat content), then let the outer handler turn it into no reply.
      window <- context
        .prompt(msg.chatId, botId)
        .handleErrorWith(error =>
          log.error(
            s"[ChatLlmResponder] could not build the prompt for ${msg.chatId}: ${error.getClass.getSimpleName}"
          ) *>
            MonadThrow[F].raiseError[String](error)
        )
      prompt = builder.build(persona, entries, window, msg)
      _         <- phase.set(ResponderPhase.Calling(prompt))
      clientAt  <- Clock[F].monotonic
      attempted <- client.complete(prompt).attempt
      finished  <- Clock[F].monotonic
      latency = (finished - clientAt).toMillis
      reply <- attempted match {
        case Left(error) =>
          // The failure is recorded (class only: the message may hold chat content), then handled as before below.
          // `Done` right before the observation, so a cancel in between still records `cancelled`, never nothing; after it the
          // observation runs in its own fiber, so a cancel adds no `cancelled` record and does not cut this one
          phase.set(ResponderPhase.Done) *> observeOwned(
            msg = msg,
            prompt = prompt,
            latencyMs = latency,
            outcome = LlmOutcome.ClientFailed(error.getClass.getSimpleName)
          ) *>
            MonadThrow[F].raiseError[Option[Reply]](error)
        case Right(completion) =>
          answer(
            msg = msg,
            prompt = prompt,
            latencyMs = latency,
            completion = completion,
            offered = offered,
            phase = phase
          )
      }
    } yield reply).handleErrorWith { error =>
      // Never the message: an exception from the client or the store can carry chat content. The phase-specific line
      // above, the observer's `client_error` and the engine's log give the rest of the context.
      log.error(s"[ChatLlmResponder] failed: ${error.getClass.getSimpleName}").as(none[Reply])
    }
  }

  private def answer(
      msg: Message,
      prompt: String,
      latencyMs: Long,
      completion: Completion,
      offered: Map[String, ReplyBundleMessage],
      phase: Ref[F, ResponderPhase]
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
      LogWriter.info(
        decisionLine(msg = msg, completion = completion, decision = decision, reuseOffered = reuseOffered)
      ) *>
      // `Done` right before the observation: from here the completed record owns the interaction, and it is written in
      // its own fiber, so a cancel neither adds a `cancelled` record nor cuts this one
      phase.set(ResponderPhase.Done) *>
      observeOwned(
        msg = msg,
        prompt = prompt,
        latencyMs = latencyMs,
        outcome = LlmOutcome.Completed(completion, decision, reuseOffered)
      ).as(
        toReply(decision, offered)
      )
  }

  /** The decision in plain text, including why there was no reply. The interaction line has the same facts in JSON;
    * this one is readable without a LogQL pipeline and carries the output-guard rule that dropped a text.
    */
  private def decisionLine(
      msg: Message,
      completion: Completion,
      decision: LlmDecision,
      reuseOffered: Boolean
  ): String = {
    val outcome = decision match {
      case LlmDecision.Text(_)                   => "text reply"
      case LlmDecision.Reuse(id) if reuseOffered => s"reuse $id"
      case LlmDecision.Reuse(id)                 => s"no reply: reuse_not_offered ($id)"
      case LlmDecision.Silent                    =>
        s"no reply: ${silenceReason(completion = completion, decision = decision, reuseOffered = reuseOffered)}"
    }
    s"[ChatLlmResponder] ${msg.chatId}: $outcome"
  }

  private def silenceReason(completion: Completion, decision: LlmDecision, reuseOffered: Boolean): String = {
    val guardReason = completion.reply
      .filter(r => r.action == com.benkio.chatllm.llm.LlmAction.Text && decision == LlmDecision.Silent)
      .flatMap(r => OutputGuard.check(r.text, builder.maxReplyChars).left.toOption)
      .map(reason => s"output_rejected:$reason")
    guardReason
      .orElse(LlmInteraction.failureOf(completion, decision, reuseOffered))
      // the model chose the `silent` action: that is a decision, not a failure
      .getOrElse("silent_action")
  }

  /** Records the interaction of a call that returned (a completion or a client failure) in its own fiber and waits for
    * it: a cancel of the caller (the engine's timeout) abandons the wait, never the write, so the record is complete
    * exactly once, and the reply is still released without waiting for a slow trace write.
    */
  private def observeOwned(msg: Message, prompt: String, latencyMs: Long, outcome: LlmOutcome): F[Unit] =
    observe(msg = msg, prompt = prompt, latencyMs = latencyMs, outcome = outcome).start.flatMap(_.joinWithNever)

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
    log.warn(s"[ChatLlmResponder] could not remember the $what of ${msg.chatId}: ${error.getClass.getSimpleName}")

  private def toReply(decision: LlmDecision, offered: Map[String, ReplyBundleMessage]): Option[Reply] =
    decision match {
      case LlmDecision.Text(text) => Some(TextReply.fromList(text)(replyToMessage = true))
      case LlmDecision.Reuse(id)  => offered.get(id).map(_.reply)
      case LlmDecision.Silent     => None
    }
}

/** Where a responder sequence is, for the record of a cancellation. `Done` means the client call returned: from then on
  * the success or failure path owns the interaction record and a cancel must not add a second one.
  */
private enum ResponderPhase {
  case Typing
  case BuildingPrompt
  case Calling(prompt: String)
  case Done
}

private object ResponderPhase {

  /** The label and prompt to record a cancel in this phase, none when it was already recorded. */
  def cancelled(phase: ResponderPhase): Option[(String, String)] = phase match {
    case Typing         => Some(("typing", ""))
    case BuildingPrompt => Some(("prompt", ""))
    case Calling(p)     => Some(("client", p))
    case Done           => None
  }
}
