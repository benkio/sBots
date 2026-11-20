package com.benkio.chattelegramadapter.http

import com.benkio.chatcore.model.ChatId
import com.benkio.chattelegramadapter.model.TelegramIds.telegramId
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.high.Api
import telegramium.bots.high.Methods
import telegramium.bots.ChatIntId

/** Reads information about a chat from Telegram. */
object TelegramChatInfo {

  /** The number of members of the chat, bots and the bot itself included. */
  def memberCount[F[_]](chatId: ChatId)(using api: Api[F]): F[Int] =
    Methods.getChatMemberCount(ChatIntId(chatId.telegramId)).exec
}
