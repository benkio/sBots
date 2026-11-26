package com.benkio.chatcore

import cats.Applicative
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.UsageStats.Outcome

/** The LLM section of the [[BotEngine]]: the single entry point where the LLM is called. It runs after, and outside,
  * the request timeout, for every message that was handled (not a duplicate). For now it does nothing: the LLM replies
  * are being rebuilt.
  */
private[chatcore] trait EngineLlm[F[_]] {

  /** Hands the handled message to the LLM. Takes the outcome of the reactive part and returns the final outcome of the
    * message: [[Outcome.LLMReplied]] when the LLM answered, the given one otherwise.
    */
  def run(msg: Message, outcome: Outcome)(using platform: PlatformAdapter[F]): F[Outcome]
}

private[chatcore] object EngineLlm {

  def noop[F[_]: Applicative]: EngineLlm[F] = new EngineLlm[F] {
    override def run(msg: Message, outcome: Outcome)(using platform: PlatformAdapter[F]): F[Outcome] =
      Applicative[F].pure(outcome)
  }
}
