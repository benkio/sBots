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
    botName: String,
    cron: CronExpr,
    subscribedAt: Instant
)

object Subscription {
  def apply(chatId: Long, botName: String, inputCron: String): Either[Throwable, Subscription] = for {
    cron <- Cron(inputCron)
  } yield Subscription(
    id = UUID.randomUUID,
    chatId = chatId,
    botName = botName,
    cron = cron,
    subscribedAt = Instant.now()
  )

  def apply(dbSubscriptionData: DBSubscriptionData): Either[Throwable, Subscription] = for {
    cron         <- Cron(dbSubscriptionData.cron)
    id           <- Try(UUID.fromString(dbSubscriptionData.id)).toEither
    subscribedAt <- Try(Instant.ofEpochSecond(dbSubscriptionData.subscribed_at.toLong)).toEither
  } yield Subscription(
    id = id,
    chatId = dbSubscriptionData.chat_id.toLong,
    botName = dbSubscriptionData.bot_name,
    cron = cron,
    subscribedAt = subscribedAt
  )

}
