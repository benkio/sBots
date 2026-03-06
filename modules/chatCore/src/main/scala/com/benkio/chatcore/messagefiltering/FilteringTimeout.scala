package com.benkio.chatcore.messagefiltering

import cats.implicits.*
import cats.MonadThrow
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.Timeout
import com.benkio.chatcore.repository.db.DBLayer

object FilteringTimeout {
  def filter[F[_]: MonadThrow](dbLayer: DBLayer[F], sBotId: SBotId): (ReplyBundleMessage, Message) => F[Boolean] =
    (_: ReplyBundleMessage, msg: Message) =>
      for {
        dbTimeout <- dbLayer.dbTimeout.getOrDefault(msg.chatId.value, sBotId)
        timeout   <- MonadThrow[F].fromEither(Timeout(dbTimeout))
      } yield Timeout.isExpired(timeout)
}
