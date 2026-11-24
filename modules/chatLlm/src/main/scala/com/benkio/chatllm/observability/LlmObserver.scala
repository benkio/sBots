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

/** What the responder knows about one finished completion. */
final case class LlmObservation(
    chatId: ChatId,
    botId: SBotId,
    promptVersion: String,
    prompt: String,
    completion: Completion,
    decision: LlmDecision,
    reuseOffered: Boolean,
    latencyMs: Long
)

/** #1493: records one interaction. The `[llm]` log line is always written; the full prompt and raw response go to the
  * `llm_trace` table only when the debug trace is enabled. Observing never fails the reply: errors are only logged.
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
                  model = modelOfCompletion(o.completion),
                  status = interaction.status,
                  action = interaction.action.label,
                  prompt = o.prompt,
                  response = o.completion.raw.map(_.text)
                )
              )
            )
        (line *> trace).handleErrorWith(e => log.warn(s"[llm] could not record the interaction: ${e.getMessage}"))
      }

      private def modelOfCompletion(c: Completion): Option[String] =
        LlmInteraction.responsibleProvider(c).flatMap(modelOf)

      private def interactionOf(o: LlmObservation): LlmInteraction = {
        val (action, reused) = LlmInteraction.actionOf(o.decision, o.reuseOffered)
        val c                = o.completion
        LlmInteraction(
          chatHash = LlmInteraction.chatHash(o.chatId, salt),
          bot = o.botId.value,
          model = modelOfCompletion(c).getOrElse("-"),
          promptVersion = o.promptVersion,
          promptTokens = LlmInteraction.estimateTokens(o.prompt),
          responseTokens = c.raw.fold(LlmInteraction.replyText(c.reply))(r => LlmInteraction.estimateTokens(r.text)),
          latencyMs = o.latencyMs,
          status = c.status.toString.toLowerCase,
          action = action,
          reusedReplyId = reused,
          failure = LlmInteraction.failureOf(c, o.decision, o.reuseOffered),
          attempts = LlmInteraction.attemptLabels(c),
          fallback = LlmInteraction.usedFallback(c),
          parsed = c.reply.isDefined,
          stripped = c.raw.exists(_.stripped),
          localRejection = c.localRejection.map(_.label)
        )
      }
    }
}
