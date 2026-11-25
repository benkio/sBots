package com.benkio.chatllm.observability

import cats.effect.Clock
import cats.syntax.all.*
import cats.Monad
import cats.MonadThrow
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatllm.llm.Completion
import com.benkio.chatllm.llm.LlmProvider
import com.benkio.chatllm.llm.ProviderResult
import com.benkio.chatllm.prompt.LlmDecision
import log.effect.LogWriter

/** How the call of the client ended: with a completion (whatever its status) or with an exception, which is not a
  * completion and is never dressed up as one. `reason` of a failure is the exception class: its message may hold chat
  * content.
  */
enum LlmOutcome {
  case Completed(completion: Completion, decision: LlmDecision, reuseOffered: Boolean)
  case ClientFailed(reason: String)

  /** The interaction was cancelled before it ended (the engine's timeout, a shutdown): no completion, no decision. */
  case Cancelled

  /** Cancelled during the client call after some provider attempts had finished: the interaction keeps them (a primary
    * timeout recorded before the fallback was cancelled), with their timings.
    */
  case CancelledAfterAttempts(partial: Completion)
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

  /** An ERROR line per provider that answered with something the structured reply could not be read from, with what it
    * returned, so the cause is visible without the debug trace. The text is the model's reply and may echo chat
    * content: it is logged in full and stays in the server logs.
    */
  def malformedReplyLines(o: LlmObservation, modelOf: LlmProvider => Option[String]): List[String] =
    o.outcome match {
      case LlmOutcome.Completed(c, _, _) =>
        c.attempts
          .filter(_.result.contains(ProviderResult.MalformedJson))
          .map { a =>
            val raw       = c.raw.filter(_.provider == a.provider)
            val diagnosis = a.failure.flatMap(_.message)
            val text      = raw.map(_.text).map(_.replaceAll("\\s+", " "))
            // the provider's diagnosis (finish reason, error object) goes with the text it explains
            val got = (text, diagnosis) match {
              case (Some(t), Some(d)) => s"$t [$d]"
              case (Some(t), None)    => t
              case (None, Some(d))    => d
              case (None, None)       => "nothing readable"
            }
            val model = raw.flatMap(_.model).orElse(modelOf(a.provider)).getOrElse("-")
            s"[LlmMalformedJson] provider=${a.provider.toString.toLowerCase} model=$model bot=${o.botId.value} got: $got"
          }
      case _ => Nil
    }

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
                    case LlmOutcome.Completed(c, _, _)        => c.raw.map(_.text)
                    case LlmOutcome.ClientFailed(_)           => None
                    case LlmOutcome.Cancelled                 => None
                    case LlmOutcome.CancelledAfterAttempts(_) => None
                  }
                )
              )
            )
        // Each sink is independent: a failed log line must not prevent the trace, the malformed-reply lines or the
        // other way round, and none may fail the reply, not even when the warning about the failure cannot be logged.
        // Only the exception class is logged: its message can carry chat content.
        val line      = bestEffort(log.info(LlmInteractionLog.line(interaction)), "record the interaction")
        val malformed =
          malformedReplyLines(o, modelOf).traverse_(l => bestEffort(log.error(l), "log a malformed reply"))
        line *> malformed *> bestEffort(trace, "store the trace")
      }

      private def bestEffort(action: F[Unit], what: String): F[Unit] =
        action.attempt.flatMap {
          case Right(_) => Monad[F].unit
          case Left(e)  =>
            log.warn(s"[llm] could not $what: ${e.getClass.getSimpleName}").handleError(_ => ())
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
          localRejection = None,
          providerErrors = Nil,
          providerErrorMessage = None,
          geminiMs = None,
          openRouterMs = None
        )
        o.outcome match {
          case LlmOutcome.ClientFailed(reason)      => base.copy(failure = Some(s"client_error:$reason"))
          case LlmOutcome.Cancelled                 => base.copy(status = "cancelled", failure = Some("cancelled"))
          case LlmOutcome.CancelledAfterAttempts(c) =>
            val usage = LlmInteraction.reportedUsage(c)
            base.copy(
              status = "cancelled",
              // a retained provider timeout is the cause worth querying; without one it stays a plain cancel
              failure =
                Some(if c.attempts.exists(_.result.contains(ProviderResult.Timeout)) then "timeout" else "cancelled"),
              attempts = LlmInteraction.attemptLabels(c),
              fallback = LlmInteraction.usedFallback(c),
              providerErrors = LlmInteraction.providerErrors(c),
              geminiMs = LlmInteraction.providerLatencyMs(c, LlmProvider.Gemini),
              openRouterMs = LlmInteraction.providerLatencyMs(c, LlmProvider.OpenRouter),
              promptTokensReported = usage.flatMap(_.prompt),
              responseTokensReported = usage.flatMap(_.completion),
              cachedTokens = usage.flatMap(_.cached)
            )
          case LlmOutcome.Completed(c, decision, reuseOffered) =>
            val (action, reused) = LlmInteraction.actionOf(decision, reuseOffered)
            val usage            = LlmInteraction.reportedUsage(c)
            base.copy(
              // the model the provider says it used (the free router picks one per request), else the configured one
              model = LlmInteraction
                .servedModel(c)
                .orElse(LlmInteraction.responsibleProvider(c).flatMap(modelOf))
                .getOrElse("-"),
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
              truncated = c.raw.exists(_.truncated),
              promptTokensReported = usage.flatMap(_.prompt),
              responseTokensReported = usage.flatMap(_.completion),
              cachedTokens = usage.flatMap(_.cached),
              localRejection = c.localRejection.map(_.label),
              providerErrors = LlmInteraction.providerErrors(c),
              providerErrorMessage = LlmInteraction.providerErrorMessage(c),
              geminiMs = LlmInteraction.providerLatencyMs(c, LlmProvider.Gemini),
              openRouterMs = LlmInteraction.providerLatencyMs(c, LlmProvider.OpenRouter)
            )
        }
      }
    }
}
