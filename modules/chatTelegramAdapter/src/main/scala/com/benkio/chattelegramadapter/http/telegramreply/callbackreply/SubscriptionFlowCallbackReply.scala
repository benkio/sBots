package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.Message as ModelMessage
import com.benkio.chatcore.model.SubscriptionFlowAction
import com.benkio.chatcore.SubscriptionFlowPromptStore
import com.benkio.chattelegramadapter.conversions.MessageConversions.toModelMessage
import com.benkio.chattelegramadapter.http.TelegramCallbackAnswer
import com.benkio.chattelegramadapter.model.TelegramIds
import com.benkio.chattelegramadapter.model.TelegramMessageIds
import telegramium.bots.high.Api
import telegramium.bots.CallbackQuery

/** Checks who pressed a guided-subscription prompt and turns an owner's press into the neutral engine action. The
  * adapter owns this check: the engine only sees an action once the pressing user is known to be the one who started
  * the flow.
  */
object SubscriptionFlowCallbackReply {

  private val unavailableText = "This prompt is not available anymore."
  private val notOwnerText    = "Only who started this subscription setup can use these buttons."

  /** `submitAction` is the engine entry point for the action, called only for the owner of a live prompt. */
  def reply[F[_]: Async: Api](
      query: CallbackQuery,
      action: SubscriptionFlowAction,
      flowPrompts: SubscriptionFlowPromptStore[F],
      submitAction: (ModelMessage, SubscriptionFlowAction, String) => F[Unit]
  ): F[Unit] =
    query.message.fold(TelegramCallbackAnswer.reject(query.id, unavailableText)) { message =>
      val requester = query.from.id.toString
      val ids       = TelegramMessageIds.getIds(message)
      val chatId    = TelegramIds.chatId(ids.chatId)
      flowPrompts.byMessageId(chatId, ids.messageId.toString).flatMap {
        case None =>
          TelegramCallbackAnswer.reject(query.id, unavailableText)
        case Some(handle) if handle.owner != requester =>
          TelegramCallbackAnswer.reject(query.id, notOwnerText)
        case Some(_) =>
          message.toModelMessage.fold(TelegramCallbackAnswer.reject(query.id, unavailableText)) { msg =>
            val model = msg.copy(authorId = requester.some)
            TelegramCallbackAnswer.ack(query.id) *> submitAction(model, action, requester)
          }
      }
    }
}
