package com.benkio.richardphjbensonbot.model

import cats.implicits._
import telegramium.bots.Message

import java.sql.Timestamp
import java.time.Instant
import scala.concurrent.duration._
import scala.util.Try
final case class Timeout(chat_id: Long, timeout_value: String, last_interaction: Timestamp)

object Timeout {
  def isExpired(timeout: Timeout): Boolean = {
    val now = Timestamp.from(Instant.now())
    now.after(
      new Timestamp(timeout.last_interaction.getTime() + timeout.timeout_value.toInt)
    )
  }

  def defaultTimeout(chatId: Long): Timeout = Timeout(
    chat_id = chatId,
    timeout_value = "0",
    last_interaction = Timestamp.from(Instant.now())
  )

  def timeStringToDuration(timeout: String): FiniteDuration =
    timeout
      .split(":")
      .map(_.toLong)
      .zip(List(HOURS, MINUTES, SECONDS))
      .map { case (value, timeUnit) =>
        FiniteDuration(value, timeUnit)
      }
      .reduce(_ + _)


  def apply(m: Message, timeout: String): Option[Timeout] =
    Try(timeStringToDuration(timeout))
      .map(duration => defaultTimeout(m.chat.id).copy(timeout_value = duration.toMillis.toString))
      .toOption

}
