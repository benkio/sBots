package com.benkio.telegrambotinfrastructure.messagefiltering

import telegramium.bots.Message
object MessageOps {
  def isCommand(msg: Message): Boolean =
    msg.text.fold(false)(t => t.startsWith("/"))
}
