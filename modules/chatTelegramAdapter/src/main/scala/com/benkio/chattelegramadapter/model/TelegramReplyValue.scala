package com.benkio.chattelegramadapter.model

import com.benkio.chatcore.model.reply.ReplyValue
import telegramium.bots.InlineKeyboardMarkup

final case class TelegramReplyValue(inlineKeyboard: InlineKeyboardMarkup) extends ReplyValue
