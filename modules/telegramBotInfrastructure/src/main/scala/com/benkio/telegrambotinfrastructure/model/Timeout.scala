package com.benkio.telegrambotinfrastructure.model

import com.benkio.telegrambotinfrastructure.repository.db.DBTimeoutData

import java.time.Instant
import scala.concurrent.duration.*
import scala.util.Try

final case class Timeout(
    chatId: ChatId,
    botId: SBotId,
    timeoutValue: FiniteDuration,
    lastInteraction: Instant
)

object Timeout {

  def apply(chatId: ChatId, botId: SBotId, timeoutValue: String): Either[Throwable, Timeout] =
    Try(timeStringToDuration(timeoutValue))
      .map(timeoutValue =>
        Timeout(
          chatId = chatId,
          botId = botId,
          timeoutValue = timeoutValue,
          lastInteraction = Instant.now()
        )
      )
      .toEither

  def apply(chatId: ChatId, botId: SBotId): Timeout = Timeout(
    chatId = chatId,
    botId = botId,
    timeoutValue = 0.millis,
    lastInteraction = Instant.now()
  )

  def apply(dbTimeoutData: DBTimeoutData): Either[Throwable, Timeout] = for {
    timeoutValue    <- Try(dbTimeoutData.timeout_value.toLong.millis).toEither
    lastInteraction <- Try(dbTimeoutData.last_interaction.toLong).toEither
  } yield Timeout(
    chatId = ChatId(dbTimeoutData.chat_id),
    botId = SBotId(dbTimeoutData.bot_id),
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
