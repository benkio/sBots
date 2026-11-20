package com.benkio.chatcore.patterns

import cats.Applicative
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBTimeout

object PostComputationPatterns {

  /** The welcome to the bot being added is not an interaction: the automatic timeout is measured from the join. */
  def timeoutPostComputation[F[_]: Applicative](dbTimeout: DBTimeout[F], sBotId: SBotId): Message => F[Unit] = m =>
    if m.botAdded then Applicative[F].unit else dbTimeout.logLastInteraction(m.chatId, sBotId)
}
