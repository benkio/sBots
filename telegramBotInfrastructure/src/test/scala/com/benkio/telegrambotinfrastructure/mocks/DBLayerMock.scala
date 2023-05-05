package com.benkio.telegrambotinfrastructure.mocks

import cats.effect.IO
import cats.effect.kernel.Ref
import com.benkio.telegrambotinfrastructure.model.ShowQuery
import com.benkio.telegrambotinfrastructure.model.Timeout
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.db.DBMedia
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import com.benkio.telegrambotinfrastructure.resources.db.DBShow
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import com.benkio.telegrambotinfrastructure.resources.db.DBSubscription
import com.benkio.telegrambotinfrastructure.resources.db.DBSubscriptionData
import com.benkio.telegrambotinfrastructure.resources.db.DBTimeout
import com.benkio.telegrambotinfrastructure.resources.db.DBTimeoutData
import doobie._

import java.time.Instant
import java.util.UUID

object DBLayerMock {
  def mock(
      botName: String,
      timeouts: List[DBTimeoutData] = List.empty,
      medias: List[DBMediaData] = List.empty,
      subscriptions: List[DBSubscriptionData] = List.empty,
      shows: List[DBShowData] = List.empty
  ): DBLayer[IO] = DBLayer(
    dbTimeout = new DBTimeoutMock(Ref.unsafe(timeouts), botName),
    dbMedia = new DBMediaMock(Ref.unsafe(medias)),
    dbSubscription = new DBSubscriptionMock(Ref.unsafe(subscriptions)),
    dbShow = new DBShowMock(Ref.unsafe(shows))
  )

  class DBTimeoutMock(db: Ref[IO, List[DBTimeoutData]], botNameI: String) extends DBTimeout[IO] {
    override def getOrDefault(chatId: Long, botName: String): IO[DBTimeoutData] =
      if (botName == botNameI)
        db.get.map(_.find(t => t.chat_id == chatId).getOrElse(DBTimeoutData(Timeout(chatId, botName))))
      else IO.raiseError(new Throwable(s"Unexpected botName, actual: $botName - expected: $botNameI"))
    override def setTimeout(timeout: DBTimeoutData): IO[Unit] =
      db.update(ts => ts.filterNot(t => t.chat_id == timeout.chat_id) :+ timeout)
    override def logLastInteraction(chatId: Long, botName: String): IO[Unit] =
      if (botName == botNameI)
        db.update(ts =>
          ts.find(t => t.chat_id == chatId)
            .fold(ts)(oldValue =>
              ts.filterNot(_ == oldValue) :+ oldValue.copy(last_interaction = Instant.now().getEpochSecond().toString)
            )
        )
      else IO.raiseError(new Throwable(s"Unexpected botName, actual: $botName - expected: $botNameI"))

    override def getOrDefaultQuery(chatId: Long, botName: String): Query0[DBTimeoutData] = ???
    override def setTimeoutQuery(timeout: DBTimeoutData): Update0                        = ???
    override def logLastInteractionQuery(chatId: Long, botName: String): Update0         = ???
  }

  class DBMediaMock(db: Ref[IO, List[DBMediaData]]) extends DBMedia[IO] {
    override def getMedia(filename: String, cache: Boolean = true): IO[DBMediaData] =
      db.get.flatMap(
        _.find(m => m.media_name == filename)
          .fold[IO[DBMediaData]](IO.raiseError(new Throwable(s"[TEST ERROR] Media not found: $filename")))(IO.pure)
      )
    override def getMediaByKind(kind: String, cache: Boolean = true): IO[List[DBMediaData]] =
      db.get.map(
        _.filter(m => m.kind.fold(false)(_ == kind))
      )
    override def getMediaByMediaCount(
        limit: Int = 20,
        mediaNamePrefix: Option[String] = None
    ): IO[List[DBMediaData]] =
      db.get.map(
        _.filter(m => mediaNamePrefix.fold(true)(pr => m.media_name.startsWith(pr)))
          .sortBy(_.media_count)(Ordering.Int.reverse)
          .take(limit)
      )
    override def incrementMediaCount(filename: String): IO[Unit] =
      db.update(ms =>
        ms.find(m => m.media_name == filename)
          .fold(ms)(oldValue => ms.filterNot(_ == oldValue) :+ oldValue.copy(media_count = oldValue.media_count + 1))
      )
    override def decrementMediaCount(filename: String): IO[Unit] =
      db.update(ms =>
        ms.find(m => m.media_name == filename)
          .fold(ms)(oldValue => ms.filterNot(_ == oldValue) :+ oldValue.copy(media_count = oldValue.media_count + 1))
      )

    override def getMediaQueryByName(resourceName: String): Query0[DBMediaData]                  = ???
    override def getMediaQueryByKind(kind: String): Query0[DBMediaData]                          = ???
    override def getMediaQueryByMediaCount(mediaNamePrefix: Option[String]): Query0[DBMediaData] = ???
  }

  class DBSubscriptionMock(db: Ref[IO, List[DBSubscriptionData]]) extends DBSubscription[IO] {
    override def getSubscriptions(botName: String, chatId: Option[Long] = None): IO[List[DBSubscriptionData]] =
      db.get.map(_.filter(subs => subs.bot_name == botName && chatId.fold(true)(_ == subs.chat_id)))
    override def insertSubscription(subscription: DBSubscriptionData): IO[Unit] =
      db.update((subs: List[DBSubscriptionData]) =>
        if (subs.exists((s: DBSubscriptionData) => s.id == subscription.id))
          throw new Throwable(s"[TEST ERROR] Subscription id already present when inserting")
        else subs :+ subscription
      )
    override def deleteSubscription(
        subscriptionId: UUID
    ): IO[Unit] =
      db.update((subs: List[DBSubscriptionData]) => subs.filterNot((s: DBSubscriptionData) => s.id == subscriptionId))
    override def deleteSubscriptions(
        chatId: Long
    ): IO[Unit] =
      db.update((subs: List[DBSubscriptionData]) => subs.filterNot((s: DBSubscriptionData) => s.chat_id == chatId))
    override def getSubscription(id: String): IO[Option[DBSubscriptionData]] =
      db.get.map(_.find(sub => sub.id.toString == id))

    override def getSubscriptionsQuery(botName: String, chatId: Option[Long] = None): Query0[DBSubscriptionData] = ???
    override def insertSubscriptionQuery(subscription: DBSubscriptionData): Update0                              = ???
    override def deleteSubscriptionQuery(subscriptionId: String): Update0                                        = ???
    override def deleteSubscriptionsQuery(chatId: Long): Update0                                                 = ???
    override def getSubscriptionQuery(id: String): Query0[DBSubscriptionData]                                    = ???
  }
  class DBShowMock(db: Ref[IO, List[DBShowData]]) extends DBShow[IO] {
    override def getShows(botName: String): IO[List[DBShowData]] =
      db.get.map(_.filter(s => s.bot_name == botName))
    override def getShowByShowQuery(query: ShowQuery, botName: String): IO[List[DBShowData]] =
      ???

    override def getShowsQuery(botName: String): Query0[DBShowData]                             = ???
    override def getShowByShowQueryQuery(query: ShowQuery, botName: String): Query0[DBShowData] = ???
  }
}
