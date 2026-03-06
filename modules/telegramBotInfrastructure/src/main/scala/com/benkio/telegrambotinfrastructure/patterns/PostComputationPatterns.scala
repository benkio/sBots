package com.benkio.telegrambotinfrastructure.patterns

import com.benkio.telegrambotinfrastructure.model.Message
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.repository.db.DBTimeout

object PostComputationPatterns {

  def timeoutPostComputation[F[_]](dbTimeout: DBTimeout[F], sBotId: SBotId): Message => F[Unit] = m =>
    dbTimeout.logLastInteraction(m.chatId.value, sBotId)
}
