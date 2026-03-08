package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import cats.effect.Async
import cats.syntax.all.*
import cats.MonadThrow
import com.benkio.chatcore.model.media.Media
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.db.DBMedia
import com.benkio.chattelegramadapter.conversions.ToInlineButton
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.KeyboardReply
import com.benkio.chattelegramadapter.model.TelegramMessageIds
import log.effect.LogWriter
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.high.Api
import telegramium.bots.high.Methods
import telegramium.bots.ChatIntId
import telegramium.bots.MaybeInaccessibleMessage

object Pagination {

  def reply[F[_]: Async: LogWriter: Api](
      msg: MaybeInaccessibleMessage,
      newPage: Int,
      dbMedia: DBMedia[F],
      sBotInfo: SBotInfo
  ): F[Unit] = {
    val telegramMessageIds = TelegramMessageIds.getIds(msg)
    for {
      _        <- LogWriter.info(s"[Pagination.reply] reply to callback for page $newPage")
      dbMedias <- dbMedia.getMediaByMediaCount(botId = sBotInfo.botId.some)
      medias   <- MonadThrow[F].fromEither(dbMedias.traverse(Media.apply))
      _        <- LogWriter.info(s"[Pagination.reply] retrieved top twenty medias: ${medias.length}")
      _        <- Methods
        .editMessageReplyMarkup(
          chatId = Some(ChatIntId(telegramMessageIds.chatId)),
          messageId = Some(telegramMessageIds.messageId),
          replyMarkup = Some(KeyboardReply.buildInlineKeyboard(medias, newPage))
        )
        .exec
    } yield ()
  }
}
