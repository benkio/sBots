package com.benkio.telegrambotinfrastructure.model

import cats.syntax.all.*
import cats.ApplicativeThrow
import cats.Show
import com.benkio.telegrambotinfrastructure.resources.db.DBSubscriptionData
import cron4s.*
import cron4s.expr.CronExpr

import java.time.Instant
import java.util.UUID
import scala.util.Try

opaque type SubscriptionId = UUID
object SubscriptionId:
  def apply(id: UUID): SubscriptionId                        = id
  extension (subscriptionId: SubscriptionId) def value: UUID = subscriptionId

final case class Subscription(
    id: SubscriptionId,
    chatId: ChatId,
    botName: String,
    cron: CronExpr,
    subscribedAt: Instant
)

object Subscription {
  def apply[F[_]: ApplicativeThrow](chatId: Long, botName: String, inputCron: String): F[Subscription] =
    ApplicativeThrow[F]
      .fromEither(Cron(inputCron))
      .map(cronExpr =>
        Subscription(
          id = SubscriptionId(UUID.randomUUID),
          chatId = ChatId(chatId),
          botName = botName,
          cron = cronExpr,
          subscribedAt = Instant.now()
        )
      )

  def apply(dbSubscriptionData: DBSubscriptionData): Either[Throwable, Subscription] = (for {
    id           <- Try(UUID.fromString(dbSubscriptionData.id))
    subscribedAt <- Try(Instant.ofEpochSecond(dbSubscriptionData.subscribed_at.toLong))
    cronExpr     <- Cron(dbSubscriptionData.cron).toTry
  } yield Subscription(
    id = SubscriptionId(id),
    chatId = ChatId(dbSubscriptionData.chat_id.toLong),
    botName = dbSubscriptionData.bot_name,
    cron = cronExpr,
    subscribedAt = subscribedAt
  )).toEither

  given showInstance: Show[Subscription] =
    Show.show(s => s"Subscription Id: ${s.id} - cron value: ${s.cron}")

}
