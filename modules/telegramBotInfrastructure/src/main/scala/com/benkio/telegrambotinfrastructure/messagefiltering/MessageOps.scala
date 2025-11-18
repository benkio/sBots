package com.benkio.telegrambotinfrastructure.messagefiltering

import com.benkio.telegrambotinfrastructure.model.MessageType
import com.benkio.telegrambotinfrastructure.model.SBotId
import telegramium.bots.Message

extension (msg: Message) {
  def messageType(botId: SBotId): MessageType =
    msg.text.fold(MessageType.Message)(t =>
      (t.startsWith("/"), t.startsWith(botId.value)) match {
        case (true, _) => MessageType.Command
        case (_, true) => MessageType.FileRequest
        case _         => MessageType.Message
      }
    )
  def getContent: Option[String] =
    msg.text.orElse(msg.caption)
}
