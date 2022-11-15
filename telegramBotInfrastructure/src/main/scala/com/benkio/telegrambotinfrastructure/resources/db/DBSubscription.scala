package com.benkio.telegrambotinfrastructure.resources.db

import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.Subscription
import doobie._
import doobie.implicits._
import doobie.util.fragments
import log.effect.LogWriter

import java.util.UUID

final case class DBSubscriptionData(
    id: String,
    chat_id: Int,
    cron: String,
    subscribed_at: String
)

object DBSubscriptionData {
  def apply(subscription: Subscription): DBSubscriptionData =
    DBSubscriptionData(
      id = subscription.id.toString,
      chat_id = subscription.chatId.toInt,
      cron = subscription.cron.toString,
      subscribed_at = subscription.subscribedAt.getEpochSecond.toString
    )
}

trait DBSubscription[F[_]] {
  def getSubscriptions(): F[List[DBSubscriptionData]]
  def insertSubscription(subscription: DBSubscriptionData): F[Unit]
  def deleteSubscription(
      subscriptionId: UUID
  ): F[Unit]

  def getSubscriptionsQuery(): Query0[DBSubscriptionData]
}

object DBSubscription {

  def apply[F[_]: Async](
      transactor: Transactor[F],
  )(implicit log: LogWriter[F]): DBSubscription[F] =
    new DBSubscriptionImpl[F](
      transactor = transactor,
      log = log
    )

  private[telegrambotinfrastructure] class DBSubscriptionImpl[F[_]: Async](
      transactor: Transactor[F],
      log: LogWriter[F]
  ) extends DBSubscription[F] {

    override def getSubscriptionsQuery(): Query0[DBSubscriptionData] =
      sql"SELECT subscription_id, chat_id, cron, subscribed_at FROM subscription".query[DBSubscriptionData]

    def insertSubscriptionQuery(subscription: DBSubscriptionData): Update0 =
      sql"INSERT INTO subscription (subscription_id, chat_id, cron, subscribed_at) VALUES (${fragments.values(subscription)})".update

    def deleteSubscriptionQuery(subscriptionId: String): Update0 =
      sql"DELETE FROM subscription WHERE subscription_id = $subscriptionId".update

    override def insertSubscription(subscription: DBSubscriptionData): F[Unit] =
      insertSubscriptionQuery(subscription).run.transact(transactor).void <* log.info(
        s"Inserted subscription $subscription"
      )

    override def deleteSubscription(
        subscriptionId: UUID
    ): F[Unit] =
      deleteSubscriptionQuery(subscriptionId.toString).run.transact(transactor).void <* log.info(
        s"delete subscription id $subscriptionId"
      )

    override def getSubscriptions(): F[List[DBSubscriptionData]] =
      getSubscriptionsQuery().stream.compile.toList.transact(transactor) <* log.info(s"Get subscriptions")

  }
}
