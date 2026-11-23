package com.benkio.chatllm.llm

import cats.effect.implicits.*
import cats.effect.Async
import cats.effect.Clock
import cats.syntax.all.*
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Notifier

/** The client shared by all the bots: it checks credentials and the local budget before asking a provider and applies
  * the configured request timeout. Missing credentials, oversized prompts and timeouts are terminal `Failed`
  * completions; an exhausted quota is a `Silent` completion and tells ops once.
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
            // Without both provider keys, or with an oversized prompt, no request is admitted: no provider is called
            // and no budget slot is consumed.
            if !LlmConfig.credentialsComplete(config) || estimateTokens(prompt) > config.maxInputTokens then Completion
              .notRequested(prompt, provider, CompletionStatus.Failed)
              .pure[F]
            else
              for {
                now      <- Clock[F].realTimeInstant
                decision <- limiter.acquire(now)
                result   <- decision match {
                  case RateLimitDecision.Allowed =>
                    underlying.complete(prompt).timeout(config.timeout).attempt.map {
                      case Right(completion) => completion
                      case Left(_)           =>
                        Completion.notRequested(prompt, provider, CompletionStatus.Failed)
                    }
                  case RateLimitDecision.Exhausted(window, firstNotice) =>
                    val message = s"LLM quota exhausted (${window.toString.toLowerCase} budget): no reply sent"
                    notifier
                      .info(opsBotInfo, message)
                      .whenA(firstNotice)
                      // A failed notification must not consume the one-shot notice: the next denial retries it.
                      .handleErrorWith(_ => limiter.notificationFailed)
                      .as(Completion.notRequested(prompt, provider, CompletionStatus.Silent))
                }
              } yield result
        } yield result
    }

  /** A cheap upper-bound-free estimate: four characters per token. The providers do the exact tokenisation; this only
    * avoids sending a prompt that is obviously beyond the configured input limit.
    */
  private def estimateTokens(prompt: String): Int =
    math.ceil(prompt.length.toDouble / 4).toInt
}
