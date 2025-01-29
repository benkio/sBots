package com.benkio.telegrambotinfrastructure.model

import cats.Show
import com.benkio.telegrambotinfrastructure.resources.db.DBSubscriptionData

import java.time.Instant
import java.util.UUID
import scala.util.Try
import little.time.CronSchedule

final case class Subscription(
    id: UUID,
    chatId: Long,
    botName: String,
    cron: String,
    cronScheduler: CronSchedule,
    subscribedAt: Instant
)

object Subscription {
  def apply(chatId: Long, botName: String, inputCron: String): Either[Throwable, Subscription] = for {
    cronScheduler <- Try(CronSchedule(inputCron)).toEither
  } yield Subscription(
    id = UUID.randomUUID,
    chatId = chatId,
    botName = botName,
    cron = inputCron,
    cronScheduler = cronScheduler,
    subscribedAt = Instant.now()
  )

  def apply(dbSubscriptionData: DBSubscriptionData): Either[Throwable, Subscription] = (for {
    cronScheduler <- Try(CronSchedule(dbSubscriptionData.cron))
    id            <- Try(UUID.fromString(dbSubscriptionData.id))
    subscribedAt  <- Try(Instant.ofEpochSecond(dbSubscriptionData.subscribed_at.toLong))
  } yield Subscription(
    id = id,
    chatId = dbSubscriptionData.chat_id.toLong,
    botName = dbSubscriptionData.bot_name,
    cron = dbSubscriptionData.cron,
    cronScheduler = cronScheduler,
    subscribedAt = subscribedAt
  )).toEither

  given showInstance: Show[Subscription] =
    Show.show(s => s"Subscription Id: ${s.id} - cron value: ${s.cron}")

}
