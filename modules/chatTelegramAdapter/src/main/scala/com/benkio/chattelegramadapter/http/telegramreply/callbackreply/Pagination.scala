package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import cats.*
import cats.effect.Async
import cats.implicits.*
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message as ModelMessage
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chattelegramadapter.conversions.MessageConversions.*
import com.benkio.chattelegramadapter.model.extractInput
import com.benkio.chattelegramadapter.model.TelegramInlineKeyboard
import com.benkio.chattelegramadapter.model.TelegramKeyboardTitle
import com.benkio.chattelegramadapter.model.TelegramMessageIds
import com.benkio.chattelegramadapter.ComputeReply
import log.effect.LogWriter
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.high.Api
import telegramium.bots.high.Methods
import telegramium.bots.ChatIntId
import telegramium.bots.MaybeInaccessibleMessage

import scala.concurrent.duration.FiniteDuration

private[callbackreply] final case class PaginationContext(
    telegramMessageIds: TelegramMessageIds,
    commandReply: ReplyBundleCommand,
    modelMessage: ModelMessage
)

object Pagination {

  private[callbackreply] def commandMessageText(commandKey: CommandKey, input: String): String =
    s"/${commandKey.asString} $input"

  private[callbackreply] def prepare(
      msg: MaybeInaccessibleMessage,
      commandKey: CommandKey,
      allCommandRepliesData: List[ReplyBundleCommand]
  ): Either[Throwable, PaginationContext] = for {
    commandReply <- ReplyBundleCommand
      .from(commandKey, allCommandRepliesData)
      .toRight(new Throwable(s"[Pagination.reply] Command reply not found for commandKey: $commandKey"))
    modelMessage <- msg.toModelMessage
      .map(m =>
        m.copy(text = {
          val input = TelegramKeyboardTitle.toTelegramKeyboardTitle(m, commandKey).extractInput
          commandMessageText(commandKey, input).some
        })
      )
      .toRight(new Throwable("[Pagination.reply] Unknown message type for pagination callback"))
  } yield PaginationContext(
    telegramMessageIds = TelegramMessageIds.getIds(msg),
    commandReply = commandReply,
    modelMessage = modelMessage
  )

  def reply[F[_]: Async: LogWriter: Api](
      msg: MaybeInaccessibleMessage,
      newPage: Int,
      commandKey: CommandKey,
      allCommandRepliesData: List[ReplyBundleCommand],
      backgroundJobManager: BackgroundJobManager[F],
      effectfulCallbacks: Map[String, ModelMessage => F[ReplyValue]],
      dbLayer: DBLayer[F],
      ttl: Option[FiniteDuration]
  ): F[Unit] =
    for {
      _ <- LogWriter.info(s"[Pagination.reply] Reply to callback for page $newPage")
      _ <- LogWriter.info(s"[Pagination.reply] Get Command Reply Data from CommandKey: $commandKey")
      paginationContext <- MonadThrow[F].fromEither(
        prepare(msg, commandKey, allCommandRepliesData)
      )
      _          <- LogWriter.info(s"[Pagination.reply] Run Reply: ${paginationContext.commandReply.reply}")
      replyValue <- ComputeReply.runReply(
        reply = paginationContext.commandReply.reply,
        msg = paginationContext.modelMessage,
        backgroundJobManager = backgroundJobManager,
        dbLayer = dbLayer,
        effectfulCallbacks = effectfulCallbacks,
        ttl = ttl,
        overridePage = Some(newPage)
      )
      telegramReplyValue <- MonadThrow[F].fromOption(
        TelegramInlineKeyboard.from(replyValue),
        new Throwable(s"[Pagination.reply] Expected TelegramInlineKeyboard reply value, got: $replyValue")
      )
      _ <- LogWriter.info("[Pagination.reply] Edit Markup Message")
      _ <- Methods
        .editMessageReplyMarkup(
          chatId = Some(ChatIntId(paginationContext.telegramMessageIds.chatId)),
          messageId = Some(paginationContext.telegramMessageIds.messageId),
          replyMarkup = Some(
            telegramReplyValue.inlineKeyboard
          )
        )
        .exec
    } yield ()

}
