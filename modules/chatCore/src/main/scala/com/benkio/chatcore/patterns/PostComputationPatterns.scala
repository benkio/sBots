package com.benkio.chatcore.patterns

import cats.implicits.*
import cats.ApplicativeThrow
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.db.DBTimeout
import com.benkio.chatcore.Notifier

object PostComputationPatterns {

  /** The welcome to the bot being added is not an interaction: the automatic timeout is measured from the join.
    *
    * A failed update leaves the stored timeout row unchanged and is reported to the operators: inserting a zero-timeout
    * row here would silently disable the chat timeout. The failure never breaks the handling of the message.
    */
  def timeoutPostComputation[F[_]: ApplicativeThrow](
      dbTimeout: DBTimeout[F],
      sBotInfo: SBotInfo,
      notifier: Notifier[F]
  ): Message => F[Unit] =
    m =>
      if m.botAdded then ().pure[F]
      else
        dbTimeout
          .logLastInteraction(m.chatId, sBotInfo.botId)
          .handleErrorWith(e =>
            notifier.error(
              sBotInfo,
              s"logging the last interaction failed (chatId=${m.chatId.id})",
              e
            )
          )
}
