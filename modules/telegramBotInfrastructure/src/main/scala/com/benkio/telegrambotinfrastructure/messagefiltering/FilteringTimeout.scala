package com.benkio.telegrambotinfrastructure.messagefiltering

import cats.implicits.*
import cats.MonadThrow
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.model.Timeout
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import telegramium.bots.Message

object FilteringTimeout {
  def filter[F[_]: MonadThrow](dbLayer: DBLayer[F], sBotId: SBotId): (ReplyBundleMessage, Message) => F[Boolean] =
    (_: ReplyBundleMessage, msg: Message) =>
      for {
        dbTimeout <- dbLayer.dbTimeout.getOrDefault(msg.chat.id, sBotId)
        timeout   <- MonadThrow[F].fromEither(Timeout(dbTimeout))
      } yield Timeout.isExpired(timeout)
}
