package com.benkio.chatcore.repository.db

import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionType
import doobie.*
import doobie.implicits.*
import doobie.util.fragments
import log.effect.LogWriter

import java.util.UUID

final case class DBSubscriptionData(
    id: String,
    platform: String,
    chat_id: String,
    bot_id: String,
    subscription_type: String = SubscriptionType.Show.asString,
    target: Option[String] = None,
    cron: String,
    subscribed_at: String
)

object DBSubscriptionData {
  def apply(subscription: Subscription): DBSubscriptionData =
    DBSubscriptionData(
      id = subscription.id.value.toString,
      platform = subscription.chatId.platform.id,
      chat_id = subscription.chatId.id,
      bot_id = subscription.botId.value,
      subscription_type = subscription.subscriptionType.asString,
      target = subscription.target,
      cron = subscription.cron.toString,
      subscribed_at = subscription.subscribedAt.getEpochSecond.toString
    )
}

trait DBSubscription[F[_]] {

  /** The subscriptions of the bot, optionally only the ones of a chat and/or of a platform. */
  def getSubscriptions(
      botId: SBotId,
      chatId: Option[ChatId] = None,
      platform: Option[Platform] = None
  ): F[List[DBSubscriptionData]]
  def getSubscription(id: String): F[Option[DBSubscriptionData]]
  def getRandomSubscription(): F[Option[DBSubscriptionData]]
  def insertSubscription(subscription: DBSubscriptionData): F[Unit]
  def deleteSubscription(
      subscriptionId: UUID
  ): F[Unit]

  /** All the subscriptions of the chat for the given bot: the table is shared by all the bots. */
  def deleteSubscriptions(
      chatId: ChatId,
      botId: SBotId
  ): F[Unit]
  def deleteSubscriptionsByType(
      chatId: ChatId,
      botId: SBotId,
      subscriptionType: SubscriptionType
  ): F[Unit]
}

object DBSubscription {

  def apply[F[_]: Async](
      transactor: Transactor[F]
  )(using log: LogWriter[F]): DBSubscription[F] =
    new DBSubscriptionImpl[F](
      transactor = transactor,
      log = log
    )

  private[chatcore] class DBSubscriptionImpl[F[_]: Async](
      transactor: Transactor[F],
      log: LogWriter[F]
  ) extends DBSubscription[F] {

    override def insertSubscription(subscription: DBSubscriptionData): F[Unit] =
      insertSubscriptionQuery(subscription).run.transact(transactor).void <* log.debug(
        s"Inserted subscription $subscription"
      )

    override def deleteSubscription(
        subscriptionId: UUID
    ): F[Unit] =
      deleteSubscriptionQuery(subscriptionId.toString).run.transact(transactor).void <* log.debug(
        s"delete subscription id $subscriptionId"
      )

    override def deleteSubscriptions(
        chatId: ChatId,
        botId: SBotId
    ): F[Unit] =
      deleteSubscriptionsQuery(chatId, botId).run.transact(transactor).void <* log.debug(
        s"delete subscriptions for chat id $chatId and bot id ${botId.value}"
      )

    override def deleteSubscriptionsByType(
        chatId: ChatId,
        botId: SBotId,
        subscriptionType: SubscriptionType
    ): F[Unit] =
      deleteSubscriptionsByTypeQuery(chatId = chatId, botId = botId, subscriptionType = subscriptionType).run
        .transact(transactor)
        .void <* log.debug(
        s"delete subscriptions for chat id $chatId, bot id ${botId.value} and type ${subscriptionType.asString}"
      )

    override def getSubscriptions(
        botId: SBotId,
        chatId: Option[ChatId],
        platform: Option[Platform]
    ): F[List[DBSubscriptionData]] =
      getSubscriptionsQuery(botId = botId, chatId = chatId, platform = platform).to[List].transact(transactor) <* log
        .debug(
          "Get subscriptions"
        )

    override def getRandomSubscription(): F[Option[DBSubscriptionData]] =
      getRandomSubscriptionQuery().option.transact(transactor) <* log.debug("Get random subscription")

    override def getSubscription(id: String): F[Option[DBSubscriptionData]] =
      getSubscriptionQuery(id).option.transact(transactor) <* log.debug(s"Get subscription: $id")

  }

  def getSubscriptionQuery(id: String): Query0[DBSubscriptionData] =
    sql"SELECT subscription_id, platform, chat_id, bot_id, subscription_type, target, cron, subscribed_at FROM subscription WHERE subscription_id = $id"
      .query[DBSubscriptionData]

  def getRandomSubscriptionQuery(): Query0[DBSubscriptionData] =
    sql"SELECT subscription_id, platform, chat_id, bot_id, subscription_type, target, cron, subscribed_at FROM subscription ORDER BY RANDOM() LIMIT 1"
      .query[DBSubscriptionData]

  def getSubscriptionsQuery(
      botId: SBotId,
      chatId: Option[ChatId] = None,
      platform: Option[Platform] = None
  ): Query0[DBSubscriptionData] =
    (sql"SELECT subscription_id, platform, chat_id, bot_id, subscription_type, target, cron, subscribed_at FROM subscription " ++ Fragments
      .whereAndOpt(
        fr"bot_id = ${botId.value}".some,
        chatId.map(cid => fr"platform = ${cid.platform.id} AND chat_id = ${cid.id}"),
        platform.map(p => fr"platform = ${p.id}")
      ))
      .query[DBSubscriptionData]

  def insertSubscriptionQuery(subscription: DBSubscriptionData): Update0 =
    sql"INSERT INTO subscription (subscription_id, platform, chat_id, bot_id, subscription_type, target, cron, subscribed_at) VALUES ${fragments
        .parentheses(fragments.values(subscription))}".update

  def deleteSubscriptionQuery(subscriptionId: String): Update0 =
    sql"DELETE FROM subscription WHERE subscription_id = $subscriptionId".update

  def deleteSubscriptionsQuery(chatId: ChatId, botId: SBotId): Update0 =
    sql"DELETE FROM subscription WHERE platform = ${chatId.platform.id} AND chat_id = ${chatId.id} AND bot_id = ${botId.value}".update

  def deleteSubscriptionsByTypeQuery(chatId: ChatId, botId: SBotId, subscriptionType: SubscriptionType): Update0 = {
    val subscriptionTypeValue = subscriptionType.asString
    sql"DELETE FROM subscription WHERE platform = ${chatId.platform.id} AND chat_id = ${chatId.id} AND bot_id = ${botId.value} AND subscription_type = $subscriptionTypeValue".update
  }
}
