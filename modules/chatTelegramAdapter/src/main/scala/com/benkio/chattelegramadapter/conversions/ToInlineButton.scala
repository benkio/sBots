package com.benkio.chattelegramadapter.conversions

import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import telegramium.bots.InlineKeyboardButton

trait ToInlineButton[A] {
  extension (a: A) def toInlineKeyboardButton: InlineKeyboardButton
}

object ToInlineButton {

  // TODO: Add tests
  given ToInlineButton[ReplyValue] with {
    extension (replyValue: ReplyValue) def toInlineKeyboardButton: InlineKeyboardButton =
      replyValue match {
        case text: Text                 => textToInlineButtonText.toInlineKeyboardButton(text)
        case mediaFile: MediaFile       => mediaFileToInlineButton.toInlineKeyboardButton(mediaFile)
        case effectfulKey: EffectfulKey => effectfulKeyToInlineButton.toInlineKeyboardButton(effectfulKey)
      }
  }

  given textToInlineButtonText: ToInlineButton[Text] with {
    extension (text: Text) def toInlineKeyboardButton: InlineKeyboardButton =
      InlineKeyboardButton(
        text = text.value,
        callbackData = None
      )
  }

  given mediaFileToInlineButton: ToInlineButton[MediaFile] with {
    extension (mediaFile: MediaFile) def toInlineKeyboardButton: InlineKeyboardButton =
      InlineKeyboardButton(
        text = mediaFile.filename,
        callbackData = Some(mediaFile.filename)
      )
  }

  given effectfulKeyToInlineButton: ToInlineButton[EffectfulKey] with {
    extension (effectfulKey: EffectfulKey) def toInlineKeyboardButton: InlineKeyboardButton =
      InlineKeyboardButton(
        text = effectfulKey.toString,
        callbackData = None
      )
  }
}
