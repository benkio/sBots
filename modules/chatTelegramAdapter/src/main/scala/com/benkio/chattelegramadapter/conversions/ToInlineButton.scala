package com.benkio.chattelegramadapter.conversions

import com.benkio.chatcore.model.media.Media
import telegramium.bots.InlineKeyboardButton

trait ToInlineButton[A] {
  def toInlineKeyboardButton(data: A): InlineKeyboardButton
}

object ToInlineButton {

  // TODO: Add tests
  given ToInlineButton[Media] with {
    override def toInlineKeyboardButton(data: Media): InlineKeyboardButton =
      InlineKeyboardButton(
        text = data.mediaName,
        callbackData = Some(data.mediaName)
      )
  }
}
