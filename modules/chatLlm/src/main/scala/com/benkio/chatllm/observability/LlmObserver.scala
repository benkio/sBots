package com.benkio.chatllm.observability

import cats.effect.Clock
import cats.syntax.all.*
import cats.Monad
import cats.MonadThrow
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatllm.llm.Completion
import com.benkio.chatllm.llm.LlmProvider
import com.benkio.chatllm.prompt.LlmDecision
import log.effect.LogWriter

/** How the call of the client ended: with a completion (whatever its status) or with an exception, which is not a
  * completion and is never dressed up as one. `reason` of a failure is the exception class: its message may hold chat
  * content.
  */
enum LlmOutcome {
  case Completed(completion: Completion, decision: LlmDecision, reuseOffered: Boolean)
  case ClientFailed(reason: String)

  /** The call was cancelled before it ended (the engine's timeout, a shutdown): no completion, no decision. */
  case Cancelled
}

/** What the responder knows about one finished interaction. */
final case class LlmObservation(
    chatId: ChatId,
    botId: SBotId,
    promptVersion: String,
    prompt: String,
    latencyMs: Long,
    outcome: LlmOutcome
)

/** #1493: records one interaction. The JSON interaction log line is always written; the full prompt and raw response go
  * to the `llm_trace` table only when the debug trace is enabled. Observing never fails the reply: errors are only
  * logged.
  */
trait LlmObserver[F[_]] {
  def observe(observation: LlmObservation): F[Unit]
}

object LlmObserver {

  /** `modelOf` names the model configured for a provider; `salt` is the secret of the anonymous labels. */
  def apply[F[_]: MonadThrow: Clock](
      traces: LlmTraceStore[F],
      traceEnabled: Boolean,
      salt: String,
      modelOf: LlmProvider => Option[String]
  )(using log: LogWriter[F]): LlmObserver[F] =
    new LlmObserver[F] {
      override def observe(o: LlmObservation): F[Unit] = {
        val interaction = interactionOf(o)
        val line        = log.info(LlmInteractionLog.line(interaction))
        val trace       =
          if !traceEnabled then Monad[F].unit
          else
            Clock[F].realTime.flatMap(now =>
              traces.append(
                LlmTrace(
                  createdAt = now.toSeconds,
                  chatHash = interaction.chatHash,
                  botId = o.botId.value,
                  promptVersion = o.promptVersion,
                  model = Some(interaction.model).filter(_ != "-"),
                  status = interaction.status,
                  action = interaction.action.label,
                  prompt = o.prompt,
                  response = o.outcome match {
                    case LlmOutcome.Completed(c, _, _) => c.raw.map(_.text)
                    case LlmOutcome.ClientFailed(_)    => None
                    case LlmOutcome.Cancelled          => None
                  }
                )
              )
            )
        // Recording never fails the reply: not even when the warning about a failed recording cannot be logged.
        (line *> trace).handleErrorWith(e =>
          log.warn(s"[llm] could not record the interaction: ${e.getMessage}").handleError(_ => ())
        )
      }

      private def interactionOf(o: LlmObservation): LlmInteraction = {
        val base = LlmInteraction(
          chatHash = LlmInteraction.chatHash(o.chatId, salt),
          bot = o.botId.value,
          model = "-",
          promptVersion = o.promptVersion,
          promptTokens = LlmInteraction.estimateTokens(o.prompt),
          responseTokens = 0,
          latencyMs = o.latencyMs,
          status = "error",
          action = ReplyAction.Silent,
          reusedReplyId = None,
          failure = None,
          attempts = Nil,
          fallback = false,
          parsed = false,
          stripped = false,
          localRejection = None
        )
        o.outcome match {
          case LlmOutcome.ClientFailed(reason) => base.copy(failure = Some(s"client_error:$reason"))
          case LlmOutcome.Cancelled            => base.copy(status = "cancelled", failure = Some("cancelled"))
          case LlmOutcome.Completed(c, decision, reuseOffered) =>
            val (action, reused) = LlmInteraction.actionOf(decision, reuseOffered)
            base.copy(
              model = LlmInteraction.responsibleProvider(c).flatMap(modelOf).getOrElse("-"),
              responseTokens =
                c.raw.fold(LlmInteraction.replyText(c.reply))(r => LlmInteraction.estimateTokens(r.text)),
              status = c.status.toString.toLowerCase,
              action = action,
              reusedReplyId = reused,
              failure = LlmInteraction.failureOf(c, decision, reuseOffered),
              attempts = LlmInteraction.attemptLabels(c),
              fallback = LlmInteraction.usedFallback(c),
              parsed = c.reply.isDefined,
              stripped = c.raw.exists(_.stripped),
              localRejection = c.localRejection.map(_.label)
            )
        }
      }
    }
}
