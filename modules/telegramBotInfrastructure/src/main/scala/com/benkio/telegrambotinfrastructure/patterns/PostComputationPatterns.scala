package com.benkio.telegrambotinfrastructure.patterns

import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.repository.db.DBTimeout
import telegramium.bots.Message

object PostComputationPatterns {

  def timeoutPostComputation[F[_]](dbTimeout: DBTimeout[F], botId: SBotId): Message => F[Unit] = m =>
    dbTimeout.logLastInteraction(m.chat.id, botId)
}
