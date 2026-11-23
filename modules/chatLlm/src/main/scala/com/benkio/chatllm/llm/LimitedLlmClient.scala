package com.benkio.chatllm.llm

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Notifier

/** The client shared by all the bots: it refuses missing credentials and oversized prompts locally, and turns a silent
  * completion (both providers' quotas exhausted) into exactly one ops message per exhaustion episode. Provider calls,
  * their quotas and the fallback are owned by the underlying client. A provider timeout is already a terminal result;
  * unexpected errors are reported to ops and propagated.
  */
object LimitedLlmClient {

  /** The limiter is not tied to a bot: its notifications are attributed to the LLM feature itself. */
  val opsBotInfo: SBotInfo =
    SBotInfo(SBotInfo.SBotId("llm"), SBotInfo.SBotName("LLM"))

  def apply[F[_]: Async](
      underlying: LlmClientApi[F],
      config: LlmConfig,
      limiter: LlmRateLimiter[F],
      notifier: Notifier[F]
  ): LlmClientApi[F] =
    new LlmClientApi[F] {

      override def complete(prompt: String): F[Completion] =
        for {
          provider <- Async[F].fromEither(
            LlmConfig.primary(config).leftMap(msg => new IllegalArgumentException(msg))
          )
          result <-
            // Without both provider keys, or with an oversized prompt, no provider is called and no quota is consumed.
            if !LlmConfig.credentialsComplete(config) || estimateTokens(prompt) > config.maxInputTokens then Completion
              .notRequested(prompt, provider, CompletionStatus.Failed)
              .pure[F]
            else
              underlying
                .complete(prompt)
                .flatMap { completion =>
                  if completion.status == CompletionStatus.Silent then notifyOnce.as(completion)
                  else completion.pure[F]
                }
                .handleErrorWith { error =>
                  // Only provider timeouts have already become terminal results. Anything else is unexpected:
                  // report it, then let it propagate instead of silently pretending no provider was asked.
                  notifier
                    .error(opsBotInfo, "LLM completion failed", error)
                    .attempt
                    .void *> Async[F].raiseError[Completion](error)
                }
        } yield result

      /** One ops message per exhaustion episode across both providers. A failed notification does not consume the
        * one-shot notice: the next silent completion retries it.
        */
      private def notifyOnce: F[Unit] =
        limiter.claimQuotaNotice.flatMap { claimed =>
          notifier
            .info(opsBotInfo, "LLM quota exhausted (both providers): no reply sent")
            .whenA(claimed)
            .handleErrorWith(_ => limiter.notificationFailed)
        }
    }

  /** A cheap upper-bound-free estimate: four characters per token. The providers do the exact tokenisation; this only
    * avoids sending a prompt that is obviously beyond the configured input limit.
    */
  private def estimateTokens(prompt: String): Int =
    math.ceil(prompt.length.toDouble / 4).toInt
}
