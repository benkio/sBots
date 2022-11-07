package com.benkio.telegrambotinfrastructure.resources.db

import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.Subscription
import doobie._
import doobie.implicits._
import doobie.util.fragments
import log.effect.LogWriter

trait DBSubscription[F[_]] {
  def getSubscriptions(): F[List[Subscription]]
  def insertSubscription(subscription: Subscription): F[Unit]
  def deleteSubscription(
      subscriptionId: Int
  ): F[Unit]

  def getSubscriptionsQuery(): Query0[Subscription]
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

    override def getSubscriptionsQuery(): Query0[Subscription] =
      sql"SELECT subscription_id, chat_id, cron, subscribed_at FROM subscription".query[Subscription]

    def insertSubscriptionQuery(subscription: Subscription): Update0 =
      sql"INSERT INTO subscription (subscription_id, chat_id, cron, subscribed_at) VALUES (${fragments.values(subscription)})".update

    def deleteSubscriptionQuery(subscriptionId: Int): Update0 =
      sql"DELETE FROM subscription WHERE subscription_id = $subscriptionId".update

    override def insertSubscription(subscription: Subscription): F[Unit] =
      insertSubscriptionQuery(subscription).run.transact(transactor).void <* log.info(
        s"Inserted subscription $subscription"
      )

    override def deleteSubscription(
        subscriptionId: Int
    ): F[Unit] =
      deleteSubscriptionQuery(subscriptionId).run.transact(transactor).void <* log.info(
        s"delete subscription id $subscriptionId"
      )

    override def getSubscriptions(): F[List[Subscription]] =
      getSubscriptionsQuery().stream.compile.toList.transact(transactor) <* log.info(s"Get subscriptions")

  }
}
