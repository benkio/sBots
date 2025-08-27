package com.benkio.telegrambotinfrastructure.http

import com.benkio.telegrambotinfrastructure.model.reply.Text.TextType
import com.benkio.telegrambotinfrastructure.model.reply.Text
import telegramium.bots.Chat
import telegramium.bots.Message
/*

Workaround in case of Error a message will be sent to me. At least being parametrized
Ideally we should have a separate support chat for that and/or a dedicated Dashboard

*/
object ErrorFallbackWorkaround:

  val supportmessage: Message =
    Message(
        messageId = 0,
        date = 0,
        chat = Chat(id = (-4145546019L), `type` = "private")
      ) // Only the chat id matters here
  def errorText(value: String): Text =
    Text(value, TextType.Markdown)
