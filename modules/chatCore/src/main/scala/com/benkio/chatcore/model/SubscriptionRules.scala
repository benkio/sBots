package com.benkio.chatcore.model

import cron4s.expr.CronExpr
import cron4s.lib.javatime.*
import cron4s.toDateTimeCronOps

import java.time.temporal.ChronoUnit
import java.time.LocalDateTime
import scala.concurrent.duration.*

/** Guardrails applied to the subscriptions created by the users. */
object SubscriptionRules {

  val minInterval: FiniteDuration  = 1.minute
  val maxSubscriptionsPerChat: Int = 50
  val maxTargetLength: Int         = 200

  // How many consecutive occurrences are checked to find the shortest gap of an irregular cron.
  private val sampledOccurrences = 120

  final case class IntervalTooShort(cron: CronExpr, shortestGap: FiniteDuration)
      extends Throwable(
        s"The subscription interval is too short: '$cron' fires every ${shortestGap.toSeconds}s, the minimum is ${minInterval.toSeconds}s"
      )
  final case class TargetTooLong(length: Int)
      extends Throwable(s"The subscription target is too long: $length characters, the maximum is $maxTargetLength")
  final case class SubscriptionLimitReached(chatId: Long)
      extends Throwable(
        s"Subscription limit reached: a chat can have at most $maxSubscriptionsPerChat subscriptions (chat id: $chatId)"
      )
  case object CalendarAlreadyActive extends Throwable("calendar subscription already active")

  /** The cron must never fire more often than [[minInterval]]. */
  def validateInterval(cron: CronExpr, from: LocalDateTime): Either[Throwable, Unit] = {
    val occurrences: List[LocalDateTime] =
      LazyList
        .unfold(Option(from))(_.flatMap(cron.next).map(next => (next, Some(next))))
        .take(sampledOccurrences)
        .toList
    val gaps: List[Long] =
      occurrences.zip(occurrences.drop(1)).map { case (a, b) => ChronoUnit.SECONDS.between(a, b) }
    gaps.minOption
      .filter(_ < minInterval.toSeconds)
      .fold(Right(()))(shortest => Left(IntervalTooShort(cron, shortest.seconds)))
  }

  def validateTarget(target: Option[String]): Either[Throwable, Unit] =
    target
      .map(_.length)
      .filter(_ > maxTargetLength)
      .fold(Right(()))(length => Left(TargetTooLong(length)))
}
