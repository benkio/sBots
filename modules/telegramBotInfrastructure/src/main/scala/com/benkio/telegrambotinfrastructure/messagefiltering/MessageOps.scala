package com.benkio.telegrambotinfrastructure.messagefiltering

import com.benkio.telegrambotinfrastructure.model.MessageType
import telegramium.bots.Message

extension (msg: Message)
  def messageType(botPrefix: String): MessageType =
    msg.text.fold(MessageType.Message)(t =>
      (t.startsWith("/"), t.startsWith(botPrefix)) match {
        case (true, _) => MessageType.Command
        case (_, true) => MessageType.FileRequest
        case _         => MessageType.Message
      }
    )
  def getContent: Option[String] =
    msg.text.orElse(msg.caption)
