package com.benkio.telegrambotinfrastructure.mocks

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
    override def getOrDefault(chatId: Long): IO[DBTimeoutData] = ???
    override def setTimeout(timeout: DBTimeoutData): IO[Unit] = ???
    override def logLastInteraction(chatId: Long): IO[Unit] = ???
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
