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
    chat_id: Long,
    bot_name: String,
    cron: String,
    subscribed_at: String
)

object DBSubscriptionData {
  def apply(subscription: Subscription): DBSubscriptionData =
    DBSubscriptionData(
      id = subscription.id.toString,
      chat_id = subscription.chatId,
      bot_name = subscription.botName,
      cron = subscription.cron.toString,
      subscribed_at = subscription.subscribedAt.getEpochSecond.toString
    )
}

trait DBSubscription[F[_]] {
  def getSubscriptions(botName: String, chatId: Option[Long] = None): F[List[DBSubscriptionData]]
  def getSubscription(id: String): F[Option[DBSubscriptionData]]
  def insertSubscription(subscription: DBSubscriptionData): F[Unit]
  def deleteSubscription(
      subscriptionId: UUID
  ): F[Unit]
  def deleteSubscriptions(
      chatId: Long
  ): F[Unit]

  def getSubscriptionsQuery(botName: String, chatId: Option[Long] = None): Query0[DBSubscriptionData]
  def getSubscriptionQuery(id: String): Query0[DBSubscriptionData]
  def insertSubscriptionQuery(subscription: DBSubscriptionData): Update0
  def deleteSubscriptionQuery(subscriptionId: String): Update0
  def deleteSubscriptionsQuery(chatId: Long): Update0
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

    override def getSubscriptionsQuery(botName: String, chatId: Option[Long] = None): Query0[DBSubscriptionData] =
      (sql"SELECT subscription_id, chat_id, bot_name, cron, subscribed_at FROM subscription " ++ Fragments.whereAndOpt(
        fr"bot_name = $botName".some,
        chatId.map(cid => fr"chat_id = $cid")
      ))
        .query[DBSubscriptionData]

    override def insertSubscriptionQuery(subscription: DBSubscriptionData): Update0 =
      sql"INSERT INTO subscription (subscription_id, chat_id, bot_name, cron, subscribed_at) VALUES ${fragments
          .parentheses(fragments.values(subscription))}".update

    override def deleteSubscriptionQuery(subscriptionId: String): Update0 =
      sql"DELETE FROM subscription WHERE subscription_id = $subscriptionId".update

    override def deleteSubscriptionsQuery(chatId: Long): Update0 =
      sql"DELETE FROM subscription WHERE chat_id = $chatId".update

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

    override def deleteSubscriptions(
        chatId: Long
    ): F[Unit] =
      deleteSubscriptionsQuery(chatId).run.transact(transactor).void <* log.info(
        s"delete subscriptions for chat id $chatId"
      )

    override def getSubscriptions(botName: String, chatId: Option[Long]): F[List[DBSubscriptionData]] =
      getSubscriptionsQuery(botName, chatId).stream.compile.toList.transact(transactor) <* log.info(
        s"Get subscriptions"
      )

    override def getSubscription(id: String): F[Option[DBSubscriptionData]] =
      getSubscriptionQuery(id).option.transact(transactor) <* log.info(s"Get subscription: $id")
    override def getSubscriptionQuery(id: String): Query0[DBSubscriptionData] =
      sql"SELECT subscription_id, chat_id, bot_name, cron, subscribed_at FROM subscription WHERE subscription_id = $id"
        .query[DBSubscriptionData]

  }
}
