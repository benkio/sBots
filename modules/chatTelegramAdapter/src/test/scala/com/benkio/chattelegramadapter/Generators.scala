package com.benkio.chattelegramadapter

object Generators {
  import com.benkio.chatcore.model.reply.ReplyValue
  import com.benkio.chatcore.Generators.mediaFileGen
  import com.benkio.chatcore.Generators.textGen
  import com.benkio.chattelegramadapter.model.TelegramInlineKeyboard
  import org.scalacheck.Gen
  import telegramium.bots.InlineKeyboardButton
  import telegramium.bots.InlineKeyboardMarkup

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

  val telegramReplyValueGen: Gen[ReplyValue] = Gen.oneOf(textGen, mediaFileGen, telegramInlineKeyboardGen)
}
