package com.benkio.chatircadapter.model

object IrcLimits {

  /** Maximum number of characters of a message. A line of the protocol is at most 512 bytes, including the prefix the
    * server adds with the nick, user and host of the bot, and the line end: 400 characters leave room for them. It is
    * counted in characters, a text of multibyte characters can still be longer in bytes, which the outbound issue
    * handles when it sends.
    */
  val maxTextMessageLength: Int = 400
}
