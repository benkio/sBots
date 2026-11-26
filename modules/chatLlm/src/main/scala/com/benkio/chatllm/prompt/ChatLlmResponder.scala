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

/** #1491, #1594: the `LlmResponder` of a bot. It offers the model the persona and the chat context only, then turns the
  * validated decision into a normal `Reply`:
  *   - `text`: a text reply to the message;
  *   - `silent`, or anything malformed, unknown, unsafe or failed: no reply.
  */
final class ChatLlmResponder[F[_]: Concurrent: Clock](
    client: LlmClientApi[F],
    context: LlmContext[F],
    builder: PromptBuilder,
    botId: SBotId,
    persona: String,
    observer: Option[LlmObserver[F]] = None
)(using log: LogWriter[F])
    extends LlmResponder[F] {

  override def respond(msg: Message): F[Option[Reply]] =
    respondAfter(msg = msg, typing = Applicative[F].unit)

  /** The typing action, the prompt and the client call are one sequence for the cancellation record: the engine's
    * timeout can fire in any of them, and each would otherwise leave no interaction line.
    */
  override def respondAfter(msg: Message, typing: F[Unit])(using
      FlatMap[F]
  ): F[Option[Reply]] =
    for {
      started <- Clock[F].monotonic
      phase   <- Ref.of[F, ResponderPhase](ResponderPhase.Typing)
      partial <- Ref.of[F, Option[Completion]](None)
      // A cancelled interaction (the engine's own timeout, a shutdown) is recorded exactly once, whatever the phase: the
      // success and error paths never run for it. The cancellation itself is preserved, and it does not wait for the
      // recording: the observation (which can write to the trace table) runs in its own fiber, so a slow database
      // cannot delay the timeout recovery and the release of the chat claim in the engine.
      reply <- run(msg = msg, typing = typing, phase = phase, partial = partial).onCancel(
        for {
          now      <- Clock[F].monotonic
          snapshot <- phase.get
          attempts <- partial.get
          // once `Done`, the success or failure path owns the record and a cancel writes no `cancelled` one
          _ <- ResponderPhase
            .cancelled(snapshot)
            .traverse_((label, prompt) =>
              observeCancelled(
                msg = msg,
                prompt = prompt,
                phase = label,
                latencyMs = (now - started).toMillis,
                attempts = attempts
              ).start.void
            )
        } yield ()
      )
    } yield reply

  private def observeCancelled(
      msg: Message,
      prompt: String,
      phase: String,
      latencyMs: Long,
      attempts: Option[Completion]
  ): F[Unit] =
    // the phase is a fixed label, never exception text or chat content
    LogWriter.info(s"[ChatLlmResponder] ${msg.chatId}: cancelled during $phase").handleError(_ => ()) *>
      observe(
        msg = msg,
        prompt = prompt,
        latencyMs = latencyMs,
        outcome = attempts
          .filter(_.attempts.nonEmpty)
          .fold[LlmOutcome](LlmOutcome.Cancelled)(
            LlmOutcome.CancelledAfterAttempts(_)
          )
      )

  private def run(
      msg: Message,
      typing: F[Unit],
      phase: Ref[F, ResponderPhase],
      partial: Ref[F, Option[Completion]]
  ): F[Option[Reply]] = {
    (for {
      _ <- typing
      _ <- LogWriter.info(s"[ChatLlmResponder] ${msg.chatId}: asking")
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
      prompt = builder.build(persona, window, msg)
      _         <- phase.set(ResponderPhase.Calling(prompt))
      clientAt  <- Clock[F].monotonic
      attempted <- client.complete(prompt, c => partial.set(Some(c))).attempt
      finished  <- Clock[F].monotonic
      latency = (finished - clientAt).toMillis
      reply <- attempted match {
        case Left(error) =>
          // The failure is recorded (class only: the message may hold chat content), then handled as before below.
          // `Done` and the start of the observation are one uncancelable step (see `observeOwned`): a cancel before it
          // records `cancelled`, after it the observation is written and no `cancelled` one is added
          observeOwned(
            phase = phase,
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
      phase: Ref[F, ResponderPhase]
  ): F[Option[Reply]] = {
    val decision = completion.reply
      .filter(_ => completion.status == CompletionStatus.Answered)
      .fold[LlmDecision](LlmDecision.Silent)(LlmDecision.from(_, builder.maxReplyChars))
    log
      .warn(
        s"[ChatLlmResponder] no answer for ${msg.chatId}: ${completion.status}, attempts " +
          completion.attempts.map(a => s"${a.provider}=${a.result.fold("none")(_.toString)}").mkString("[", ", ", "]")
      )
      .whenA(completion.status != CompletionStatus.Answered && completion.status != CompletionStatus.Silent) *>
      LogWriter.info(
        decisionLine(msg = msg, completion = completion, decision = decision)
      ) *>
      // from `Done` on the completed record owns the interaction (see `observeOwned`): a cancel neither adds a
      // `cancelled` record nor cuts this one
      observeOwned(
        phase = phase,
        msg = msg,
        prompt = prompt,
        latencyMs = latencyMs,
        outcome = LlmOutcome.Completed(completion, decision)
      ).as(
        toReply(decision)
      )
  }

  /** The decision in plain text, including why there was no reply. The interaction line has the same facts in JSON;
    * this one is readable without a LogQL pipeline and carries the output-guard rule that dropped a text.
    */
  private def decisionLine(msg: Message, completion: Completion, decision: LlmDecision): String = {
    val outcome = decision match {
      case LlmDecision.Text(_) => "text reply"
      case LlmDecision.Silent  => s"no reply: ${silenceReason(completion = completion, decision = decision)}"
    }
    s"[ChatLlmResponder] ${msg.chatId}: $outcome"
  }

  private def silenceReason(completion: Completion, decision: LlmDecision): String = {
    val guardReason = completion.reply
      .filter(r => r.action == com.benkio.chatllm.llm.LlmAction.Text && decision == LlmDecision.Silent)
      .flatMap(r => OutputGuard.check(r.text, builder.maxReplyChars).left.toOption)
      .map(reason => s"output_rejected:$reason")
    guardReason
      .orElse(LlmInteraction.failureOf(completion, decision))
      // the model chose the `silent` action: that is a decision, not a failure
      .getOrElse("silent_action")
  }

  /** Records the interaction of a call that returned (a completion or a client failure). Setting `Done` and starting
    * the observation in its own fiber are one uncancelable step, so a cancel lands either before it (recorded as
    * `cancelled`) or after it (the observation is written, nothing else is). Cancellation is restored only while
    * waiting for the observation: a cancel there abandons the wait, never the write, and the reply is still released
    * without waiting for a slow trace write.
    */
  private def observeOwned(
      phase: Ref[F, ResponderPhase],
      msg: Message,
      prompt: String,
      latencyMs: Long,
      outcome: LlmOutcome
  ): F[Unit] =
    Concurrent[F].uncancelable { poll =>
      phase.set(ResponderPhase.Done) *>
        observe(msg = msg, prompt = prompt, latencyMs = latencyMs, outcome = outcome).start
          .flatMap(fiber => poll(fiber.joinWithNever))
    }

  private def observe(msg: Message, prompt: String, latencyMs: Long, outcome: LlmOutcome): F[Unit] =
    observer.traverse_(
      _.observe(
        LlmObservation(
          chatId = msg.chatId,
          botId = botId,
          prompt = prompt,
          latencyMs = latencyMs,
          outcome = outcome
        )
      )
    )

  override def remember(msg: Message)(using Applicative[F]): F[Unit] =
    context.remember(msg, botId).void.handleErrorWith(rememberFailed("message", msg))

  /** A text reply is remembered as the text sent. */
  override def rememberAnswer(msg: Message, reply: Reply)(using Applicative[F]): F[Unit] = {
    val content = reply match {
      case TextReply(texts, _) => texts.headOption.map(_.value)
      case _                   => None
    }
    content.traverse_(text => context.rememberAssistant(msg.chatId, botId, text).void)
  }.handleErrorWith(rememberFailed("answer", msg))

  private def rememberFailed(what: String, msg: Message)(error: Throwable): F[Unit] =
    log.warn(s"[ChatLlmResponder] could not remember the $what of ${msg.chatId}: ${error.getClass.getSimpleName}")

  private def toReply(decision: LlmDecision): Option[Reply] =
    decision match {
      case LlmDecision.Text(text) => Some(TextReply.fromList(text)(replyToMessage = true))
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
