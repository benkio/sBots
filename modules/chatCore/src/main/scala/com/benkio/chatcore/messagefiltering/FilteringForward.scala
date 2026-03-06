package com.benkio.chatcore.messagefiltering

import com.benkio.chatcore.model.Message

object FilteringForward {
  def filter(msg: Message, disableForward: Boolean): Boolean =
    !disableForward || !msg.isForward
}
