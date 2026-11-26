package com.benkio.chattelegramadapter.http.telegramreply.messagereply

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SubscriptionFlowAction
import com.benkio.chatcore.model.SubscriptionFlowPrompt
import com.benkio.chatcore.SubscriptionFlowPromptStore
import com.benkio.chattelegramadapter.conversions.MessageConversions.replyParameters
import com.benkio.chattelegramadapter.model.TelegramIds.telegramId
import com.benkio.chattelegramadapter.model.TelegramInlineKeyboard
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.high.Api
import telegramium.bots.high.FailedRequest
import telegramium.bots.high.Methods
import telegramium.bots.ChatIntId
import telegramium.bots.InlineKeyboardButton
import telegramium.bots.InlineKeyboardMarkup
import telegramium.bots.Message as TMessage

/** Renders a guided-subscription prompt as a Telegram keyboard and keeps the same message across all of its steps.
  * Every step after the first edits the message in place; errors and the final result are ordinary new messages sent by
  * the other reply senders.
  */
object SubscriptionFlowPromptReply {

  /** Telegram refuses to edit a message that was deleted with either of these descriptions. Such a failure is
    * permanent: the stored handle has to be replaced, unlike every other edit failure.
    */
  private def isMissingMessage(error: Throwable): Boolean =
    error match {
      case FailedRequest(_, _, Some(description)) =>
        val text = description.toLowerCase
        text.contains("message to edit not found") || text.contains("message_id_invalid")
      case _ => false
    }

  /** Telegram rejects an edit whose content and markup are identical to the current message. The prompt is already in
    * the desired state, so the edit can be treated as a success.
    */
  private def isNotModified(error: Throwable): Boolean =
    error match {
      case FailedRequest(_, _, Some(description)) => description.toLowerCase.contains("message is not modified")
      case _                                      => false
    }

  /** The prompt text is the keyboard title and each button carries only the id of its neutral flow action. */
  def toKeyboard(prompt: SubscriptionFlowPrompt): TelegramInlineKeyboard = {
    val rows = prompt.buttons.map(
      _.map(button =>
        InlineKeyboardButton(text = button.label, callbackData = SubscriptionFlowAction.toId(button.action))
      )
    )
    TelegramInlineKeyboard(keyboardTitle = prompt.text, inlineKeyboard = InlineKeyboardMarkup(rows))
  }

  /** Sends the first prompt of a key, then edits that same message for every later prompt of the key. */
  def send[F[_]: Async: Api](
      prompt: SubscriptionFlowPrompt,
      msg: Message,
      replyToMessage: Boolean,
      flowPrompts: SubscriptionFlowPromptStore[F]
  ): F[List[TMessage]] = {
    val keyboard = toKeyboard(prompt)

    def fresh: F[List[TMessage]] =
      Methods
        .sendMessage(
          chatId = Some(ChatIntId(msg.chatId.telegramId)),
          text = prompt.text,
          replyParameters = msg.replyParameters(replyToMessage),
          replyMarkup = Some(keyboard.inlineKeyboard)
        )
        .exec
        .flatMap(sent =>
          flowPrompts
            .put(prompt.key, sent.messageId.toString, prompt.key.requester)
            .as(List(sent))
        )

    flowPrompts
      .byKey(prompt.key)
      .flatMap(_.fold(fresh) { handle =>
        Methods
          .editMessageText(
            chatId = Some(ChatIntId(prompt.key.chatId.telegramId)),
            messageId = Some(handle.messageId.toInt),
            text = Some(prompt.text),
            replyMarkup = Some(keyboard.inlineKeyboard)
          )
          .exec
          .flatMap(_ => flowPrompts.put(prompt.key, handle.messageId, prompt.key.requester))
          .as(List.empty[TMessage])
          .handleErrorWith {
            case error if isMissingMessage(error) => flowPrompts.remove(prompt.key) >> fresh
            case error if isNotModified(error)    =>
              flowPrompts.put(prompt.key, handle.messageId, prompt.key.requester).as(List.empty[TMessage])
            case other => Async[F].raiseError[List[TMessage]](other)
          }
      })
  }
}
