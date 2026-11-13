package com.benkio.chatircadapter.model

object IrcLimits {

  /** Maximum number of characters of a message. A line of the protocol is at most 512 bytes, including the prefix the
    * server adds with the nick, user and host of the bot, and the line end: 400 characters leave room for them. It is
    * counted in characters, a text of multibyte characters can still be longer in bytes, which the outbound issue
    * handles when it sends.
    */
  val maxTextMessageLength: Int = 400

  /** Maximum size of the text of a message in bytes, so that the line of the protocol (the prefix with the nick, user
    * and host of the bot, `PRIVMSG`, the target, the line end) stays under the 512 bytes whatever the characters are.
    */
  val maxLineBytes: Int = 400

  /** Maximum number of lines of the reply to a message: a longer text is cut, so a bot cannot fill a channel (and be
    * disconnected for flooding) with one reply.
    */
  val maxLinesPerReply: Int = 20
}
