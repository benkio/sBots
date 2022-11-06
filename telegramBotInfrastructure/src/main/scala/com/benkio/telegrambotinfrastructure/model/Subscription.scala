package com.benkio.telegrambotinfrastructure.model

final case class Subscription(id: Int, chatId: Int, cron: String, subscribedAt: String)
