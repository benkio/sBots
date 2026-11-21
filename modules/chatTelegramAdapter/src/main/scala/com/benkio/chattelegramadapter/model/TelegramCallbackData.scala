package com.benkio.chattelegramadapter.model

import com.benkio.chatcore.model.reply.PagedListAction

object TelegramCallbackData {

  /** The action of the data of a callback, `None` when the data is not a valid action id (bad format or too long). */
  def decode(callbackData: String): Option[PagedListAction] =
    PagedListAction.fromId(callbackData, TelegramCapabilities.value.commandSyntax)
}
