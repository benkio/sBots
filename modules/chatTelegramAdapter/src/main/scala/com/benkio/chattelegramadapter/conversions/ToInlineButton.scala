package com.benkio.chattelegramadapter.conversions

import com.benkio.chatcore.model.media.Media
import telegramium.bots.InlineKeyboardButton

trait ToInlineButton[A] {
  extension (a: A) def toInlineKeyboardButton(textF: A => String): InlineKeyboardButton
}

object ToInlineButton {

  // TODO: Add tests
  given ToInlineButton[Media] with {
    extension (media: Media) def toInlineKeyboardButton(textF: Media => String): InlineKeyboardButton =
      InlineKeyboardButton(
        text = textF(media),
        callbackData = Some(media.mediaName)
      )
  }
}
