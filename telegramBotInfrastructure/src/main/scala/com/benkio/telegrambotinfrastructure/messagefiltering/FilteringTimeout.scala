package com.benkio.telegrambotinfrastructure.messagefiltering

import cats.MonadThrow
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.{ReplyBundleMessage, Timeout}
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import telegramium.bots.Message

object FilteringTimeout {
  def filter[F[_]: MonadThrow](dbLayer: DBLayer[F], botName: String) : (ReplyBundleMessage[F], Message) => F[Boolean] =
    (_: ReplyBundleMessage[F], msg: Message) =>
  for {
    dbTimeout <- dbLayer.dbTimeout.getOrDefault(msg.chat.id, botName)
    timeout   <- MonadThrow[F].fromEither(Timeout(dbTimeout))
  } yield Timeout.isExpired(timeout)
}
