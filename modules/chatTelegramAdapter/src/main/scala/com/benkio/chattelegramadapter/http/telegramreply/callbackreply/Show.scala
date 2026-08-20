package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.show.Show as CoreShow
import com.benkio.chatcore.repository.db.DBShow
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.TextReply
import com.benkio.chattelegramadapter.model.TelegramMessageIds
import log.effect.LogWriter
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.high.Api
import telegramium.bots.high.Methods
import telegramium.bots.ChatIntId
import telegramium.bots.MaybeInaccessibleMessage

object Show {

  def reply[F[_]: Async: LogWriter: Api](
      msg: MaybeInaccessibleMessage,
      showId: String,
      showDb: DBShow[F]
  ): F[Unit] = {
    val telegramMessageIds = TelegramMessageIds.getIds(msg)
    val modelMsg           = ModelMessageFromCallback.build(msg)

    for {
      _           <- LogWriter.info(s"[Show.reply] reply to callback for show $showId")
      optShowData <- showDb.getShowById(showId)
      optShow     <- optShowData.traverse(CoreShow(_))
      _           <- TextReply
        .sendText(
          reply = Text(optShow.fold("Unexpected error when fetching the show")(_.show)),
          msg = modelMsg,
          replyToMessage = false
        )
      _ <- Methods
        .deleteMessage(
          chatId = ChatIntId(telegramMessageIds.chatId),
          messageId = telegramMessageIds.messageId
        )
        .exec
    } yield ()
  }
}
