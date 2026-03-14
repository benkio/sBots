package com.benkio.chattelegramadapter.http.telegramreply.messagereply

import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.messagefiltering.getContent
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chattelegramadapter.conversions.ToInlineButton
import com.benkio.chattelegramadapter.model.toCallbackKey
import com.benkio.chattelegramadapter.model.CallbackData
import com.benkio.chattelegramadapter.model.TelegramInlineKeyboard
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.ChatId
import telegramium.bots.ChatIntId
import telegramium.bots.InlineKeyboardButton
import telegramium.bots.InlineKeyboardMarkup
import telegramium.bots.Message as TMessage
import telegramium.bots.ReplyParameters

object KeyboardReply {
  def sendKeyboard[F[_]: Async: LogWriter: Api](
      reply: TelegramInlineKeyboard,
      msg: Message,
      replyToMessage: Boolean
  ): F[List[TMessage]] = {
    val chatId: ChatId            = ChatIntId(msg.chatId.value)
    val result: F[List[TMessage]] =
      for {
        _       <- LogWriter.info(s"[TelegramMessageReply[Keyboard]] reply to message: ${msg.getContent}")
        _       <- Methods.sendChatAction(chatId, "typing").exec
        message <-
          Methods
            .sendMessage(
              chatId = chatId,
              text = reply.keyboardTitle,
              replyParameters = Option.when(replyToMessage)(ReplyParameters(msg.messageId)),
              replyMarkup = Some(
                reply.inlineKeyboard
              )
            )
            .exec
      } yield List(message)
    result
      .handleErrorWith(e =>
        LogWriter.error(s"[KeyboardReply] error occurred when sending keyboard. Error: $e") *> List.empty.pure[F]
      )
  }

  def buildInlineKeyboard[A: ToInlineButton](
      data: List[A],
      page: Int,
      commandKey: CommandKey
  ): InlineKeyboardMarkup = {
    val perPage: Int = 5
    val selectedData = data.slice(page * perPage, (page + 1) * perPage)
    InlineKeyboardMarkup(
      selectedData.map(d => d.toInlineKeyboardButton.toList) :+ paginationButtons(
        page = page,
        perPage = perPage,
        totalElems = data.length,
        commandKey = commandKey
      )
    )
  }
  def paginationButtons(
      page: Int,
      perPage: Int,
      totalElems: Int,
      commandKey: CommandKey
  ): List[InlineKeyboardButton] = {
    val prev: Option[InlineKeyboardButton] = Option.unless(page == 0)(
      InlineKeyboardButton(
        text = "prev ⎗",
        callbackData = Some(CallbackData.PreviousPage(page, commandKey).toCallbackKey)
      )
    )
    val next: Option[InlineKeyboardButton] = Option.unless((page + 1) * perPage == totalElems)(
      InlineKeyboardButton(
        text = "next ⎘",
        callbackData = Some(CallbackData.NextPage(page, commandKey).toCallbackKey)
      )
    )
    prev.toList ++ next.toList
  }
}
