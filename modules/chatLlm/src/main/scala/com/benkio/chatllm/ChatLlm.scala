package com.benkio.chatllm

import cats.effect.Async
import com.benkio.chatcore.llm.LlmSettings
import com.benkio.chatcore.llm.LlmWiring
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatllm.repository.db.LlmSettingsDB
import doobie.util.transactor.Transactor
import log.effect.LogWriter

/** The implementations of the chatCore LLM ports. Only the `/llm` settings exist for now: the per-chat switch is stored
  * and read from the bot's DB. The LLM replies are being rebuilt on top of LLM4s.
  */
object ChatLlm {

  /** The settings port for one bot: the table is scoped by platform, chat and bot. */
  def dbSettings[F[_]: Async](transactor: Transactor[F], botId: SBotId)(using
      log: LogWriter[F]
  ): LlmSettings[F] =
    LlmSettingsDB[F](transactor, botId)

  def wiring[F[_]: Async](using log: LogWriter[F]): LlmWiring[F] =
    LlmWiring(settings = dbSettings[F])
}
