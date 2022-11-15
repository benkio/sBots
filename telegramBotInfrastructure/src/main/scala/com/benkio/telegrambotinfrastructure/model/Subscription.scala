package com.benkio.telegrambotinfrastructure.model

import com.benkio.telegrambotinfrastructure.resources.db.DBSubscriptionData
import cron4s.Cron
import cron4s.expr.CronExpr

import java.time.Instant
import java.util.UUID
import scala.util.Try

final case class Subscription(
    id: UUID,
    chatId: Long,
    cron: CronExpr,
    subscribedAt: Instant
)

object Subscription {
  def apply(chatId: Long, inputCron: String): Either[Throwable, Subscription] = for {
    cron <- Cron(inputCron)
  } yield Subscription(
    id = UUID.randomUUID,
    chatId = chatId,
    cron = cron,
    subscribedAt = Instant.now()
  )

  def apply(dbSubscriptionData: DBSubscriptionData): Either[Throwable, Subscription] = for {
    cron         <- Cron(dbSubscriptionData.cron)
    id           <- Try(UUID.fromString(dbSubscriptionData.id)).toEither
    subscribedAt <- Try(Instant.parse(dbSubscriptionData.subscribed_at)).toEither
  } yield Subscription(
    id = id,
    chatId = dbSubscriptionData.chat_id.toLong,
    cron = cron,
    subscribedAt = subscribedAt
  )

}
