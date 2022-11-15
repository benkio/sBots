package com.benkio.telegrambotinfrastructure.mocks

import java.sql.Timestamp
import java.time.Instant
import com.benkio.telegrambotinfrastructure.model.Timeout
import java.util.UUID
import com.benkio.telegrambotinfrastructure.resources.db.DBSubscription
import com.benkio.telegrambotinfrastructure.resources.db.DBSubscriptionData
import com.benkio.telegrambotinfrastructure.resources.db.DBMedia
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import cats.effect.kernel.Ref
import com.benkio.telegrambotinfrastructure.resources.db.DBTimeoutData
import com.benkio.telegrambotinfrastructure.resources.db.DBTimeout
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import cats.effect.IO
import doobie._

object DBLayerMock {
  def mock(
    timeouts: List[DBTimeoutData] = List.empty,
    medias: List[DBMediaData] = List.empty,
    subscriptions: List[DBSubscriptionData] = List.empty,

  ): DBLayer[IO] = DBLayer(
    dbTimeout      = new DBTimeoutMock(Ref.unsafe(timeouts)),
    dbMedia        = new DBMediaMock(Ref.unsafe(medias)),
    dbSubscription = new DBSubscriptionMock(Ref.unsafe(subscriptions))
  )

  class DBTimeoutMock(db: Ref[IO, List[DBTimeoutData]]) extends DBTimeout[IO] {
    override def getOrDefault(chatId: Long): IO[DBTimeoutData] =
      db.get.map(_.find(t => t.chat_id == chatId).getOrElse(DBTimeoutData(Timeout(chatId))))
    override def setTimeout(timeout: DBTimeoutData): IO[Unit] =
      db.update(ts => ts.filterNot(t => t.chat_id == timeout.chat_id) :+ timeout)
    override def logLastInteraction(chatId: Long): IO[Unit] =
      db.update(ts => ts.find(t => t.chat_id == chatId).fold(ts)(oldValue =>
        ts.filterNot(_ == oldValue) :+ oldValue.copy(last_interaction = Timestamp.from(Instant.now()))
      ))
  }

  class DBMediaMock(db: Ref[IO, List[DBMediaData]]) extends DBMedia[IO] {
    override def getMedia(filename: String, cache: Boolean = true): IO[DBMediaData] = ???
    override def getMediaByKind(kind: String, cache: Boolean = true): IO[List[DBMediaData]] = ???
    override def getMediaByMediaCount(
      limit: Int = 20,
      mediaNamePrefix: Option[String] = None
    ): IO[List[DBMediaData]] = ???
    override def incrementMediaCount(filename: String): IO[Unit] = ???
    override def decrementMediaCount(filename: String): IO[Unit] = ???

    override def getMediaQueryByName(resourceName: String): Query0[DBMediaData] = ???
    override def getMediaQueryByKind(kind: String): Query0[DBMediaData] = ???
    override def getMediaQueryByMediaCount(mediaNamePrefix: Option[String], limit: Int): Query0[DBMediaData] = ???
  }

  class DBSubscriptionMock(db: Ref[IO, List[DBSubscriptionData]]) extends DBSubscription[IO] {
    override def getSubscriptions(): IO[List[DBSubscriptionData]] = ???
    override def insertSubscription(subscription: DBSubscriptionData): IO[Unit] = ???
    override def deleteSubscription(
      subscriptionId: UUID
    ): IO[Unit] = ???

    override def getSubscriptionsQuery(): Query0[DBSubscriptionData] = ???
  }
}
