package com.benkio.chattelegramadapter

import telegramium.bots.MaybeInaccessibleMessage

object Generators {
  import com.benkio.chatcore.model.reply.ReplyValue
  import com.benkio.chatcore.Generators.mediaFileGen
  import com.benkio.chatcore.Generators.textGen
  import com.benkio.chattelegramadapter.model.TelegramInlineKeyboard
  import org.scalacheck.Gen
  import telegramium.bots.Chat
  import telegramium.bots.InaccessibleMessage
  import telegramium.bots.InlineKeyboardButton
  import telegramium.bots.InlineKeyboardMarkup
  import telegramium.bots.Message as TelegramMessage

  val telegramChatGen: Gen[Chat] = for {
    id       <- Gen.long
    chatType <- Gen.nonEmptyListOf(Gen.alphaNumChar).map(_.mkString)
  } yield Chat(id = id, `type` = chatType)

  val telegramMessageGen: Gen[TelegramMessage] = for {
    messageId <- Gen.choose(Int.MinValue, Int.MaxValue)
    date      <- Gen.choose(Int.MinValue, Int.MaxValue)
    chat      <- telegramChatGen
    text      <- Gen.option(Gen.alphaStr)
    caption   <- Gen.option(Gen.alphaStr)
  } yield TelegramMessage(
    messageId = messageId,
    date = date,
    chat = chat,
    text = text,
    caption = caption
  )

  val inaccessibleMessageGen: Gen[InaccessibleMessage] = for {
    messageId <- Gen.choose(Int.MinValue, Int.MaxValue)
    date      <- Gen.choose(Int.MinValue, Int.MaxValue)
    chat      <- telegramChatGen
  } yield InaccessibleMessage(messageId = messageId, date = date, chat = chat)

  val telegramInlineKeyboardGen: Gen[TelegramInlineKeyboard] = for {
    keyboardTitle         <- Gen.alphaStr
    keyboardButtonText    <- Gen.alphaStr
    keyboardButtonPayload <- Gen.alphaStr
  } yield TelegramInlineKeyboard(
    keyboardTitle = keyboardTitle,
    inlineKeyboard = InlineKeyboardMarkup(
      List(
        List(
          InlineKeyboardButton(
            text = keyboardButtonText,
            callbackData = Option.when(keyboardButtonPayload.nonEmpty)(keyboardButtonPayload)
          )
        )
      )
    )
  )

  val maybeInaccessibleMessageGen: Gen[MaybeInaccessibleMessage] = Gen.oneOf(
    telegramMessageGen,
    inaccessibleMessageGen
  )

  val telegramReplyValueGen: Gen[ReplyValue] = Gen.oneOf(textGen, mediaFileGen, telegramInlineKeyboardGen)
}
