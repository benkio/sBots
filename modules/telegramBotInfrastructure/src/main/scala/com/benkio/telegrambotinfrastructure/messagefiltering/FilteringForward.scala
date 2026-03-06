package com.benkio.telegrambotinfrastructure.messagefiltering

import com.benkio.telegrambotinfrastructure.model.Message

object FilteringForward {
  def filter(msg: Message, disableForward: Boolean): Boolean =
    disableForward == false || msg.forwardOrigin.isEmpty
}
