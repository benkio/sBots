package com.benkio.chatllm.llm

import cats.effect.syntax.monadCancel.*
import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Notifier

import scala.concurrent.duration.*

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
            if !LlmConfig.credentialsComplete(config) then Completion
              .notRequested(prompt, provider, CompletionStatus.Failed)
              .copy(localRejection = Some(LocalRejection.MissingCredentials))
              .pure[F]
            else if estimateTokens(prompt) > config.maxInputTokens then notifyKeyed(
              key = OversizedKey,
              cooldown = Some(OversizedCooldown),
              message =
                s"LLM prompt of about ${estimateTokens(prompt)} tokens exceeds the input limit of ${config.maxInputTokens}: no reply sent"
            ).as(
              Completion
                .notRequested(prompt, provider, CompletionStatus.Failed)
                .copy(localRejection = Some(LocalRejection.OversizedPrompt))
            )
            else
              underlying
                .complete(prompt)
                .flatMap(completion => report(completion).as(completion))
                .handleErrorWith { error =>
                  // Only provider timeouts have already become terminal results. Anything else is unexpected:
                  // report it, then let it propagate instead of silently pretending no provider was asked.
                  notifier
                    .error(opsBotInfo, "LLM completion failed", error)
                    .attempt
                    .void *> Async[F].raiseError[Completion](error)
                }
        } yield result

      /** Ops messages for what would otherwise be a silent failure: the quota exhaustion, and a provider that rejects
        * the request (a revoked key, a removed model), once per provider until it answers again.
        */
      private def report(completion: Completion): F[Unit] = {
        val rejectedAttempts = completion.attempts.filter(_.result.contains(ProviderResult.Rejected))
        val rejected         = rejectedAttempts.map(_.provider).distinct
        val detailOf         = (p: LlmProvider) =>
          rejectedAttempts.find(_.provider == p).flatMap(_.failure).fold("") { f =>
            s": HTTP ${f.httpStatus.toString}${f.message.fold("")(m => s" $m")}"
          }
        val answered = completion.attempts.filter(_.result.contains(ProviderResult.Success)).map(_.provider).distinct
        answered.traverse_(p => limiter.releaseNotice(rejectedKey(p))) *>
          rejected.traverse_(p =>
            notifyKeyed(
              key = rejectedKey(p),
              cooldown = None,
              message =
                s"LLM provider ${p.toString} rejected the request${detailOf(p)} (check its key, its model, and whether the prompt exceeds its input size)"
            )
          ) *>
          (if completion.status == CompletionStatus.Silent then notifyOnce else Async[F].unit)
      }

      private def rejectedKey(provider: LlmProvider): String = s"rejected:${provider.toString}"

      private def notifyKeyed(key: String, cooldown: Option[FiniteDuration], message: String): F[Unit] =
        // The claim and the cleanup are one uncancelable section, so cancellation cannot leak a claimed key. Only a key
        // this fiber claimed is released, on a failed notification and on cancellation while it is sent.
        Async[F].uncancelable { poll =>
          limiter.claimNotice(key, cooldown).flatMap { claimed =>
            if !claimed then Async[F].unit
            else
              poll(notifier.info(opsBotInfo, message))
                .onCancel(limiter.releaseNotice(key))
                .handleErrorWith(_ => limiter.releaseNotice(key))
          }
        }

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

  private val OversizedKey = "oversized-prompt"

  /** Whether a prompt is oversized depends on the chat and the message, so the notice is rate limited by time instead
    * of released by the next normal prompt. The responder logs the chat of every failed completion.
    */
  private val OversizedCooldown = 1.hour

  /** A cheap upper-bound-free estimate: four characters per token. The providers do the exact tokenisation; this only
    * avoids sending a prompt that is obviously beyond the configured input limit.
    */
  private def estimateTokens(prompt: String): Int =
    math.ceil(prompt.length.toDouble / 4).toInt
}
