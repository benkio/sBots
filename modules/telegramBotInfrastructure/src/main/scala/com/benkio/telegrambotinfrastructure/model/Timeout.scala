package com.benkio.telegrambotinfrastructure.model

import com.benkio.telegrambotinfrastructure.repository.db.DBTimeoutData

import java.time.Instant
import scala.concurrent.duration.*
import scala.util.Try

final case class Timeout(
    chatId: ChatId,
    botName: String,
    timeoutValue: FiniteDuration,
    lastInteraction: Instant
)

object Timeout {

  def apply(chatId: ChatId, botName: String, timeoutValue: String): Either[Throwable, Timeout] =
    Try(timeStringToDuration(timeoutValue))
      .map(timeoutValue =>
        Timeout(
          chatId = chatId,
          botName = botName,
          timeoutValue = timeoutValue,
          lastInteraction = Instant.now()
        )
      )
      .toEither

  def apply(chatId: ChatId, botName: String): Timeout = Timeout(
    chatId = chatId,
    botName = botName,
    timeoutValue = 0.millis,
    lastInteraction = Instant.now()
  )

  def apply(dbTimeoutData: DBTimeoutData): Either[Throwable, Timeout] = for {
    timeoutValue    <- Try(dbTimeoutData.timeout_value.toLong.millis).toEither
    lastInteraction <- Try(dbTimeoutData.last_interaction.toLong).toEither
  } yield Timeout(
    chatId = ChatId(dbTimeoutData.chat_id),
    botName = dbTimeoutData.bot_name,
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

  def formatTimeout(timeout: Timeout): String = {
    val duration   = timeout.timeoutValue
    val millis     = duration.toMillis
    val hours      = millis / (1000 * 60 * 60)
    val minutes    = (millis / (1000 * 60)) % 60
    val seconds    = (millis / 1000)        % 60
    val millisLeft = millis                 % 1000
    f"$hours%02d:$minutes%02d:$seconds%02d.$millisLeft%03d"
  }
}
