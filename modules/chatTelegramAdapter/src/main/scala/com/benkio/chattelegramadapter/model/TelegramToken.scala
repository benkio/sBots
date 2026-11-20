package com.benkio.chattelegramadapter.model

/** The token of a Telegram bot: `<bot id>:<secret>`. */
opaque type TelegramToken = String

object TelegramToken {
  def apply(value: String): TelegramToken = value

  extension (token: TelegramToken) {
    def value: String = token

    /** The id of the bot, the part of the token before the `:`. */
    def botId: Option[Long] = token.takeWhile(_ != ':').toLongOption
  }
}
