package com.benkio.chatdiscordadapter.model

object DiscordLimits {

  /** Maximum number of characters of a message. */
  val maxTextMessageLength: Int = 2000

  /** Maximum size of an uploaded file, in bytes, on a server without boosts (10 MiB). Larger servers allow more and
    * the limit is lower or higher in other contexts: it is the safe value. The capabilities do not carry a size limit
    * yet, so enforcing it (a larger file sent as a link) belongs to the outbound issue.
    */
  val maxUploadBytes: Long = 10L * 1024 * 1024
}
