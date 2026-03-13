package com.benkio.chattelegramadapter.model

import com.benkio.chatcore.model.reply.ReplyValue
import telegramium.bots.InlineKeyboardMarkup

final case class TelegramInlineKeyboard(
    keyboardTitle: String,
    inlineKeyboard: InlineKeyboardMarkup
) extends ReplyValue

object TelegramInlineKeyboard {
  def from(replyValue: ReplyValue): Option[TelegramInlineKeyboard] =
    ReplyValue.from[TelegramInlineKeyboard](replyValue)
}
