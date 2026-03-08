package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import telegramium.bots.MaybeInaccessibleMessage

object Pagination {

  def reply(msg: MaybeInaccessibleMessage): Unit = {
    ???
    // TODO:
    // re-query the db
    // change the page
    // edit the markup message
    /*
     editMessageReplyMarkup(
                chatId = Some(ChatIntId(msg.chat.id)),
                messageId = Some(msg.messageId),
                replyMarkup = None
              ).exec
     */
  }
}
