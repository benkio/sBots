package com.benkio.chatcore.messagefiltering

import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageType
import com.benkio.chatcore.model.SBotInfo.SBotId

extension (msg: Message) {

  /** What the message is, with the command syntax of the platform it comes from (`/command` unless said otherwise). */
  def messageType(botId: SBotId, syntax: CommandSyntax = CommandSyntax.Slash): MessageType =
    msg.text.fold(MessageType.Message)(t =>
      (syntax.isCommand(t), syntax.isFileRequest(t, botId)) match {
        case (true, _) => MessageType.Command
        case (_, true) => MessageType.FileRequest
        case _         => MessageType.Message
      }
    )

  def getContent: Option[String] =
    msg.text.orElse(msg.caption)
}
