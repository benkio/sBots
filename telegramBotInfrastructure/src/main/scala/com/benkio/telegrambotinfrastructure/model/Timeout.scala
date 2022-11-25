package com.benkio.telegrambotinfrastructure.model

import cats.implicits._
import com.benkio.telegrambotinfrastructure.resources.db.DBTimeoutData

import java.time.Instant
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.util.Try

final case class Timeout(
    chatId: Long,
    timeoutValue: FiniteDuration,
    lastInteraction: Instant
)

object Timeout {

  def apply(chatId: Long, timeoutValue: String): Either[Throwable, Timeout] =
    Try(timeStringToDuration(timeoutValue))
      .map(timeoutValue =>
        Timeout(
          chatId = chatId,
          timeoutValue = timeoutValue,
          lastInteraction = Instant.now()
        )
      )
      .toEither

  def apply(chatId: Long): Timeout = Timeout(
    chatId = chatId,
    timeoutValue = 0.millis,
    lastInteraction = Instant.now()
  )

  def apply(dbTimeoutData: DBTimeoutData): Either[Throwable, Timeout] = for {
    timeoutValue    <- Try(dbTimeoutData.timeout_value.toLong.millis).toEither
    lastInteraction <- Try(dbTimeoutData.last_interaction.toLong).toEither
  } yield Timeout(
    chatId = dbTimeoutData.chat_id,
    timeoutValue = timeoutValue,
    lastInteraction = Instant.ofEpochSecond(lastInteraction)
  )

  def isExpired(timeout: Timeout): Boolean =
    Instant.now().isAfter(timeout.lastInteraction.plusMillis(timeout.timeoutValue.toMillis))

  private[model] def timeStringToDuration(timeout: String): FiniteDuration =
    timeout
      .split(":")
      .map(_.toLong)
      .zip(List(HOURS, MINUTES, SECONDS))
      .map { case (value, timeUnit) =>
        FiniteDuration(value, timeUnit)
      }
      .reduce(_ + _)

}
