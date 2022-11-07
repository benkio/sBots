package com.benkio.telegrambotinfrastructure.model

import cats.implicits._
import telegramium.bots.Message

import java.time.Instant
import scala.concurrent.duration._
import scala.util.Try
import scala.concurrent.duration.FiniteDuration

final case class Timeout(
  chatId: Long,
  timeoutValue: FiniteDuration,
  lastInteraction: Instant
)

object Timeout {

  def apply(chatId: Long, timeoutValue: String): Either[Throwable, Timeout] =
    Try(timeoutValue.toLong).map(timeoutValue =>
      Timeout(
        chatId = chatId,
        timeoutValue = timeoutValue.millis,
        lastInteraction = Instant.now()
      )
    ).toEither

  def apply(chatId: Long): Timeout = Timeout(
    chatId = chatId,
    timeoutValue = 0.millis,
    lastInteraction = Instant.now()
  )

  def isExpired(timeout: Timeout): Boolean = {
    val now = Instant.now()
    now.after(
      new Timestamp(timeout.last_interaction.getTime() + timeout.timeout_value.toInt)
    )
  }

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
