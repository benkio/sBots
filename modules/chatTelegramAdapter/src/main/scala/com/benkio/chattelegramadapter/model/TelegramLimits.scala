package com.benkio.chattelegramadapter.model

/** Limits imposed by the Telegram Bot API. Platform-agnostic code (`chatCore`) receives them as parameters. */
object TelegramLimits {

  /** Maximum number of characters of a text message. */
  val maxTextMessageLength: Int = 4096
}
