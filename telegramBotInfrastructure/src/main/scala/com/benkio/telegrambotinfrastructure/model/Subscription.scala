package com.benkio.telegrambotinfrastructure.model

import java.time.Instant
import cron4s.expr.CronExpr
import cron4s.Cron
import java.util.UUID

final case class Subscription(
  id: UUID,
  chatId: Int,
  cron: CronExpr,
  subscribedAt: Instant
)

object Subscription {
  def apply(chatId: Int, inputCron: String) : Either[Throwable, Subscription] = for {
    cron <- Cron(inputCron)
  } yield Subscription(
    id = UUID.randomUUID,
    chatId = chatId,
    cron = cron,
    subscribedAt = Instant.now()
  )

}
