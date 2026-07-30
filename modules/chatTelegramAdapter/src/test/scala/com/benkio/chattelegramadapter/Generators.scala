package com.benkio.chattelegramadapter

import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chattelegramadapter.model.CallbackData
import com.benkio.chattelegramadapter.model.TelegramInlineKeyboard
import org.scalacheck.Gen
import telegramium.bots.Chat
import telegramium.bots.InaccessibleMessage
import telegramium.bots.InlineKeyboardButton
import telegramium.bots.InlineKeyboardMarkup
import telegramium.bots.MaybeInaccessibleMessage
import telegramium.bots.Message as TelegramMessage

trait Generators extends com.benkio.chatcore.Generators {

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

  /** Media payloads that do not parse as pagination keys (`previousPage|nextPage-CommandKey-Int`). */
  val mediaCallbackPayloadGen: Gen[String] =
    Gen.alphaNumStr.suchThat(s =>
      s.nonEmpty &&
        !s.startsWith("previousPage-") &&
        !s.startsWith("nextPage-")
    )

  val callbackDataGen: Gen[CallbackData] = Gen.oneOf(
    for {
      page <- Gen.choose(0, 100)
      key  <- commandKeyGen
    } yield CallbackData.PreviousPage(page, key),
    for {
      page <- Gen.choose(0, 100)
      key  <- commandKeyGen
    } yield CallbackData.NextPage(page, key),
    mediaCallbackPayloadGen.map(CallbackData.Media.apply)
  )
}

object Generators extends Generators
