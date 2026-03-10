package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.ChatId as ModelChatId
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message as ModelMessage
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chattelegramadapter.conversions.ToInlineButton
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.KeyboardReply
import com.benkio.chattelegramadapter.model.TelegramMessageIds
import log.effect.LogWriter
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.high.Api
import telegramium.bots.high.Methods
import telegramium.bots.ChatIntId
import telegramium.bots.MaybeInaccessibleMessage

import scala.concurrent.duration.FiniteDuration

object Pagination {

  def reply[F[_]: Async: LogWriter: Api](
      msg: MaybeInaccessibleMessage,
      newPage: Int,
      commandKey: CommandKey,
      sBotConfig: SBotConfig,
      repository: Repository[F],
      backgroundJobManager: BackgroundJobManager[F],
      effectfulCallbacks: Map[String, ModelMessage => F[List[Text]]],
      dbLayer: DBLayer[F],
      ttl: Option[FiniteDuration]
  ): F[Unit] = {
    val telegramMessageIds = TelegramMessageIds.getIds(msg)
    val modelMsg           =
      ModelMessage(
        messageId = telegramMessageIds.messageId,
        date = 0L,
        chatId = ModelChatId(telegramMessageIds.chatId),
        chatType = telegramMessageIds.chatType
      )
    for {
      _     <- LogWriter.info(s"[Pagination.reply] reply to callback for page $newPage")
      // TODO: use commandkey to get the commandReplyData
      // Add a function to filter
      // then the reply
      // then the data

      _ <- LogWriter.info(s"[Pagination.reply] retrieved top twenty medias: ${datas.length}")
      _ <- Methods
        .editMessageReplyMarkup(
          chatId = Some(ChatIntId(telegramMessageIds.chatId)),
          messageId = Some(telegramMessageIds.messageId),
          replyMarkup = Some(
            KeyboardReply.buildInlineKeyboard(
              data = datas,
              page = newPage,
              commandKey = commandKey
            )
          )
        )
        .exec
    } yield ()
  }
}
