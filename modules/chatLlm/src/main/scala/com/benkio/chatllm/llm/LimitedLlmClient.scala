package com.benkio.chatllm.llm

import cats.effect.Async
import cats.effect.Clock
import cats.effect.implicits.*
import cats.syntax.all.*
import com.benkio.chatcore.Notifier
import com.benkio.chatcore.model.SBotInfo

/** The client shared by all the bots: it checks the local budget before asking a provider and applies the configured
  * request timeout. When the quota is exhausted it stays silent and tells ops once.
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
          // An oversized prompt is refused before admission control: it must not consume a budget slot.
          oversized = estimateTokens(prompt) > config.maxInputTokens
          decision <- if oversized then RateLimitDecision.Allowed.pure[F]
                      else Clock[F].realTimeInstant.flatMap(limiter.acquire)
          result <- decision match {
            case RateLimitDecision.Allowed if oversized =>
              Completion.notRequested(prompt, provider, CompletionStatus.Failed).pure[F]
            case RateLimitDecision.Allowed =>
              underlying.complete(prompt).timeout(config.timeout)
            case RateLimitDecision.Exhausted(window, firstNotice) =>
              val message = s"LLM quota exhausted (${window.toString.toLowerCase} budget): no reply sent"
              notifier
                .info(opsBotInfo, message)
                .whenA(firstNotice)
                .as(Completion.notRequested(prompt, provider, CompletionStatus.Silent))
          }
        } yield result
    }

  /** A cheap upper-bound-free estimate: four characters per token. The providers do the exact tokenisation; this only
    * avoids sending a prompt that is obviously beyond the configured input limit.
    */
  private def estimateTokens(prompt: String): Int =
    math.ceil(prompt.length.toDouble / 4).toInt
}
