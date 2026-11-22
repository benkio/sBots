package com.benkio.chattelegramadapter.model

import com.benkio.chatcore.model.reply.PagedListAction
import com.benkio.chatcore.model.SubscriptionFlowAction

object TelegramCallbackData {

  /** The action of the data of a callback, `None` when the data is not a valid action id (bad format or too long). */
  def decode(callbackData: String): Option[PagedListAction] =
    PagedListAction.fromId(callbackData, TelegramCapabilities.value.commandSyntax)

  /** The guided-flow action of a callback, `None` when the data is not a valid flow action id. Flow ids are prefixed
    * with `flow-`, so they never overlap the ids of the paged lists.
    */
  def decodeFlow(callbackData: String): Option[SubscriptionFlowAction] =
    Option.when(callbackData.startsWith("flow-"))(callbackData).flatMap(SubscriptionFlowAction.fromId)
}
