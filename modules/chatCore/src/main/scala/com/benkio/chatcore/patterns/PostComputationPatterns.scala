package com.benkio.chatcore.patterns

import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBTimeout

object PostComputationPatterns {

  def timeoutPostComputation[F[_]](dbTimeout: DBTimeout[F], sBotId: SBotId): Message => F[Unit] = m =>
    dbTimeout.logLastInteraction(m.chatId.value, sBotId)
}
