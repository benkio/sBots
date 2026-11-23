package com.benkio.chatllm

import cats.Applicative
import com.benkio.chatcore.llm.LlmResponder
import com.benkio.chatcore.llm.LlmSettings

/** The LLM support of the bots: provider clients, rate limiting, prompt building and the implementation of the
  * `LlmResponder` / `LlmSettings` ports of chatCore. Only `main` depends on this module.
  *
  * For now it only hands out the `noop` ports: the feature is unconfigured.
  */
object ChatLlm {

  def responder[F[_]: Applicative]: LlmResponder[F] = LlmResponder.noop[F]
  def settings[F[_]: Applicative]: LlmSettings[F]   = LlmSettings.noop[F]
}
