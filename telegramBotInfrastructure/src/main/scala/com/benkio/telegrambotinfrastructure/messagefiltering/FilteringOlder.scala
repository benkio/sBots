package com.benkio.telegrambotinfrastructure.messagefiltering

import telegramium.bots.Message

import java.time.Instant
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

object FilteringOlder {

  val olderThreshold: FiniteDuration = 5.minutes

  def filter(msg: Message): Boolean = {
    val nowSeconds: Long = Instant.now.getEpochSecond()
    nowSeconds - msg.date.toLong <= nowSeconds - (nowSeconds - olderThreshold.toSeconds)
  }
}
