package com.benkio.chattelegramadapter.conversions

import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.reply.VideoFile
import com.benkio.chattelegramadapter.conversions.ToInlineButton.given
import com.benkio.chattelegramadapter.model.TelegramInlineKeyboard
import munit.FunSuite
import telegramium.bots.InlineKeyboardButton
import telegramium.bots.InlineKeyboardMarkup

class ToInlineKeyboardButtonSpec extends FunSuite {
  test("toInlineKeyboardButton[Text] should map to inline button without callback") {
    val text = Text("hello")
    assertEquals(
      text.toInlineKeyboardButton,
      Some(InlineKeyboardButton(text = "hello", callbackData = None))
    )
  }

  test("toInlineKeyboardButton[MediaFile] should map to inline button with filename callback") {
    val mediaFile: MediaFile = Mp3File("song.mp3")
    assertEquals(
      mediaFile.toInlineKeyboardButton,
      Some(InlineKeyboardButton(text = "song.mp3", callbackData = Some("song.mp3")))
    )
  }

  test("toInlineKeyboardButton[ReplyValue] should dispatch text values") {
    val replyValue: ReplyValue = Text("from reply value")
    assertEquals(
      replyValue.toInlineKeyboardButton,
      Some(InlineKeyboardButton(text = "from reply value", callbackData = None))
    )
  }

  test("toInlineKeyboardButton[ReplyValue] should dispatch media files") {
    val replyValue: ReplyValue = VideoFile("video.mp4")
    assertEquals(
      replyValue.toInlineKeyboardButton,
      Some(InlineKeyboardButton(text = "video.mp4", callbackData = Some("video.mp4")))
    )
  }

  test("toInlineKeyboardButton[ReplyValue] should not convert TelegramInlineKeyboard") {
    val keyboard = InlineKeyboardMarkup(
      List(
        List(
          InlineKeyboardButton(
            text = "example",
            callbackData = Some("payload")
          )
        )
      )
    )
    val replyValue: ReplyValue = TelegramInlineKeyboard(
      keyboardTitle = "title",
      inlineKeyboard = keyboard
    )
    assertEquals(replyValue.toInlineKeyboardButton, None)
  }

  test("toInlineKeyboardButton should cover each ReplyValue conversion path in one pass") {
    val keyboard = InlineKeyboardMarkup(
      List(
        List(
          InlineKeyboardButton(
            text = "example",
            callbackData = Some("payload")
          )
        )
      )
    )
    val cases: List[(ReplyValue, Option[InlineKeyboardButton])] = List(
      Text("loop")           -> Some(InlineKeyboardButton(text = "loop", callbackData = None)),
      VideoFile("media.mp4") -> Some(InlineKeyboardButton(text = "media.mp4", callbackData = Some("media.mp4"))),
      TelegramInlineKeyboard("loop kb", keyboard) -> None
    )

    cases.foreach { case (input, expected) =>
      assertEquals(input.toInlineKeyboardButton, expected)
    }
  }
}
