package com.benkio.chattelegramadapter.model

import com.benkio.chatcore.model.reply.PagedListAction

object TelegramCallbackData {

  /** The action of the data of a callback. An id that is not one of an action is a media file named by the raw data. */
  def decode(callbackData: String): PagedListAction =
    PagedListAction
      .fromId(callbackData, TelegramCapabilities.value.commandSyntax)
      .getOrElse(PagedListAction.Media(callbackData))
}
