package com.benkio.chattelegramadapter.conversions

import cats.syntax.all.*
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chattelegramadapter.model.TelegramInlineKeyboard
import telegramium.bots.InlineKeyboardButton

trait ToInlineButton[A] {
  extension (a: A) def toInlineKeyboardButton: Option[InlineKeyboardButton]
}

object ToInlineButton {

  // TODO: Add tests
  given ToInlineButton[ReplyValue] with {
    extension (replyValue: ReplyValue) def toInlineKeyboardButton: Option[InlineKeyboardButton] =
      replyValue match {
        case text: Text                => textToInlineButtonText.toInlineKeyboardButton(text)
        case mediaFile: MediaFile      => mediaFileToInlineButton.toInlineKeyboardButton(mediaFile)
        case _: TelegramInlineKeyboard => None
      }
  }

  given textToInlineButtonText: ToInlineButton[Text] with {
    extension (text: Text) def toInlineKeyboardButton: Option[InlineKeyboardButton] =
      InlineKeyboardButton(
        text = text.value,
        callbackData = None
      ).some
  }

  given mediaFileToInlineButton: ToInlineButton[MediaFile] with {
    extension (mediaFile: MediaFile) def toInlineKeyboardButton: Option[InlineKeyboardButton] =
      InlineKeyboardButton(
        text = mediaFile.filename,
        callbackData = Some(mediaFile.filename)
      ).some
  }

}
