package com.benkio.chatcore.model

import cats.syntax.all.*
import cats.ApplicativeThrow
import cats.Show
import com.benkio.chatcore.model.reply.Markdown
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBSubscriptionData
import cron4s.*
import cron4s.expr.CronExpr

import java.time.Instant
import java.util.UUID
import scala.util.Try

opaque type SubscriptionId = UUID
object SubscriptionId {
  def apply(id: UUID): SubscriptionId                        = id
  extension (subscriptionId: SubscriptionId) def value: UUID = subscriptionId
}

enum SubscriptionType(val asString: String) {
  case Show     extends SubscriptionType("show")
  case Trigger  extends SubscriptionType("trigger")
  case Calendar extends SubscriptionType("calendar")
}

object SubscriptionType {

  final case class UnknownSubscriptionType(value: String) extends Throwable(s"Unknown subscription type: $value")

  def fromString(value: String): Either[Throwable, SubscriptionType] =
    SubscriptionType.values
      .find(_.asString == value.trim.toLowerCase)
      .toRight(UnknownSubscriptionType(value))
}

final case class Subscription(
    id: SubscriptionId,
    chatId: ChatId,
    botId: SBotId,
    subscriptionType: SubscriptionType = SubscriptionType.Show,
    target: Option[String] = None,
    cron: CronExpr,
    subscribedAt: Instant
)

object Subscription {
  def apply[F[_]: ApplicativeThrow](
      chatId: ChatId,
      botId: SBotId,
      inputCron: String,
      subscriptionType: SubscriptionType,
      target: Option[String]
  ): F[Subscription] =
    ApplicativeThrow[F]
      .fromEither(Cron(inputCron))
      .map(cronExpr =>
        Subscription(
          id = SubscriptionId(UUID.randomUUID),
          chatId = chatId,
          botId = botId,
          subscriptionType = subscriptionType,
          target = target,
          cron = cronExpr,
          subscribedAt = Instant.now()
        )
      )

  def apply(dbSubscriptionData: DBSubscriptionData): Either[Throwable, Subscription] = (for {
    id               <- Try(UUID.fromString(dbSubscriptionData.id))
    chatId           <- ChatId.fromIds(dbSubscriptionData.platform, dbSubscriptionData.chat_id).toTry
    subscribedAt     <- Try(Instant.ofEpochSecond(dbSubscriptionData.subscribed_at.toLong))
    subscriptionType <- SubscriptionType.fromString(dbSubscriptionData.subscription_type).toTry
    cronExpr         <- Cron(dbSubscriptionData.cron).toTry
  } yield Subscription(
    id = SubscriptionId(id),
    chatId = chatId,
    botId = SBotId(dbSubscriptionData.bot_id),
    subscriptionType = subscriptionType,
    target = dbSubscriptionData.target,
    cron = cronExpr,
    subscribedAt = subscribedAt
  )).toEither

  given showInstance: Show[Subscription] =
    Show.show(s =>
      s"Subscription Id: `${s.id}` - Type: ${s.subscriptionType} - Cron: `${s.cron}`" ++
        s.target.fold("")(target => s" - Target: ${Markdown.escape(target)}")
    )

}
