package com.benkio.chattelegramadapter.http.telegramreply.messagereply

import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.messagefiltering.getContent
import com.benkio.chatcore.model.reply.PagedList
import com.benkio.chatcore.model.reply.ReplyValue
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
    val chatId: ChatId            = ChatIntId(msg.chatId.telegramId)
    val result: F[List[TMessage]] =
      for {
        _       <- LogWriter.info(s"[TelegramMessageReply[Keyboard]] reply to message: ${msg.getContent}")
        _       <- Methods.sendChatAction(chatId, "typing").exec
        message <-
          Methods
            .sendMessage(
              chatId = chatId,
              text = reply.keyboardTitle,
              replyParameters = Option.when(replyToMessage)(ReplyParameters(Some(msg.messageId))),
              replyMarkup = Some(
                reply.inlineKeyboard
              )
            )
            .exec
      } yield List(message)
    result
      .handleErrorWith(e =>
        LogWriter.error(
          s"[KeyboardReply] error occurred when sending keyboard. Error: $e - Keyboard: $reply"
        ) *> List.empty.pure[F]
      )
  }

  /** The paged list as the inline keyboard of Telegram: a button for every item of the page and the ones to move across
    * the pages.
    */
  def toKeyboard(pagedList: PagedList): TelegramInlineKeyboard =
    TelegramInlineKeyboard(
      keyboardTitle = pagedList.title,
      inlineKeyboard = buildInlineKeyboard(
        data = pagedList.items,
        page = pagedList.page,
        commandKey = pagedList.commandKey,
        perPage = pagedList.pageSize
      )
    )

  /** The keyboard to send for a reply value: the keyboard itself or the one of a paged list. */
  def keyboardOf(replyValue: ReplyValue): Option[TelegramInlineKeyboard] =
    TelegramInlineKeyboard
      .from(replyValue)
      .orElse(ReplyValue.from[PagedList](replyValue).map(toKeyboard))

  def buildInlineKeyboard[A: ToInlineButton](
      data: List[A],
      page: Int,
      commandKey: CommandKey,
      perPage: Int = PagedList.defaultPageSize
  ): InlineKeyboardMarkup = {
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
    val next: Option[InlineKeyboardButton] = Option.unless((page + 1) * perPage >= totalElems)(
      InlineKeyboardButton(
        text = "next ⎘",
        callbackData = Some(CallbackData.NextPage(page, commandKey).toCallbackKey)
      )
    )
    prev.toList ++ next.toList
  }
}
