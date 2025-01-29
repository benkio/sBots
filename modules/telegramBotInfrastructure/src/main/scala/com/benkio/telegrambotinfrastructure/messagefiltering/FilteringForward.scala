package com.benkio.telegrambotinfrastructure.messagefiltering

import telegramium.bots.Message

object FilteringForward {
  def filter(msg: Message, disableForward: Boolean): Boolean =
    disableForward == false || msg.forwardOrigin.isEmpty
}
