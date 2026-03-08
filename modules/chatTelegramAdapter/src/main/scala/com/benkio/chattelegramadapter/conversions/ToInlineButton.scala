package com.benkio.chattelegramadapter.conversions

import com.benkio.chatcore.model.media.Media
import telegramium.bots.InlineKeyboardButton

trait ToInlineButton[A] {
  extension (a: A) def toInlineKeyboardButton: InlineKeyboardButton
}

object ToInlineButton {

  // TODO: Add tests
  given ToInlineButton[Media] with {
    extension (media: Media) def toInlineKeyboardButton: InlineKeyboardButton =
      InlineKeyboardButton(
        text = media.mediaName,
        callbackData = Some(media.mediaName)
      )
  }
}
