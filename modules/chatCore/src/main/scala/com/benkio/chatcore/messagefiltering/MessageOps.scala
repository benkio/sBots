package com.benkio.chatcore.messagefiltering

import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageType
import com.benkio.chatcore.model.SBotInfo.SBotId

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
