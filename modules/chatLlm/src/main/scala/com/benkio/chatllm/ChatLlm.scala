package com.benkio.chatllm

import cats.effect.Async
import cats.Applicative
import com.benkio.chatcore.llm.LlmResponder
import com.benkio.chatcore.llm.LlmSettings
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatllm.repository.db.LlmContextStore
import com.benkio.chatllm.repository.db.LlmSettingsDB
import doobie.util.transactor.Transactor
import log.effect.LogWriter

/** The LLM support of the bots: provider clients, rate limiting, prompt building and the implementation of the
  * `LlmResponder` / `LlmSettings` ports of chatCore. Only `main` depends on this module.
  *
  * `settings` is still the `noop` port: the feature is unconfigured. `dbSettings` is wired by `main` behind the `/llm`
  * command (#1488); `contextStore` is the real doobie-backed context, ready for the engine routing of #1489.
  */
object ChatLlm {

  def responder[F[_]: Applicative]: LlmResponder[F] = LlmResponder.noop[F]
  def settings[F[_]: Applicative]: LlmSettings[F]   = LlmSettings.noop[F]

  /** The settings port for one bot: the table is scoped by platform, chat and bot. */
  def dbSettings[F[_]: Async](transactor: Transactor[F], botId: SBotId)(using
      log: LogWriter[F]
  ): LlmSettings[F] =
    LlmSettingsDB[F](transactor, botId)

  def contextStore[F[_]: Async](transactor: Transactor[F]): LlmContextStore[F] =
    LlmContextStore[F](transactor)
}
