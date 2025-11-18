package com.benkio.telegrambotinfrastructure.mocks

import cats.effect.kernel.Ref
import cats.effect.std.Random
import cats.effect.IO
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.model.show.ShowQuery
import com.benkio.telegrambotinfrastructure.model.ChatId
import com.benkio.telegrambotinfrastructure.model.SBotId
import com.benkio.telegrambotinfrastructure.model.Timeout
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.db.DBLog
import com.benkio.telegrambotinfrastructure.repository.db.DBLogData
import com.benkio.telegrambotinfrastructure.repository.db.DBMedia
import com.benkio.telegrambotinfrastructure.repository.db.DBMediaData
import com.benkio.telegrambotinfrastructure.repository.db.DBShow
import com.benkio.telegrambotinfrastructure.repository.db.DBShowData
import com.benkio.telegrambotinfrastructure.repository.db.DBSubscription
import com.benkio.telegrambotinfrastructure.repository.db.DBSubscriptionData
import com.benkio.telegrambotinfrastructure.repository.db.DBTimeout
import com.benkio.telegrambotinfrastructure.repository.db.DBTimeoutData

import java.time.Instant
import java.util.UUID

object DBLayerMock {
  def mock(
      botId: SBotId,
      timeouts: List[DBTimeoutData] = List.empty,
      medias: List[DBMediaData] = List.empty,
      subscriptions: List[DBSubscriptionData] = List.empty,
      shows: List[DBShowData] = List.empty,
      logs: List[DBLogData] = List.empty
  ): DBLayer[IO] = DBLayer(
    dbTimeout = DBTimeoutMock(Ref.unsafe(timeouts), botId),
    dbMedia = DBMediaMock(Ref.unsafe(medias)),
    dbSubscription = DBSubscriptionMock(Ref.unsafe(subscriptions)),
    dbShow = DBShowMock(Ref.unsafe(shows)),
    dbLog = DBLogMock(Ref.unsafe(logs))
  )

  class DBTimeoutMock(db: Ref[IO, List[DBTimeoutData]], botIdI: SBotId) extends DBTimeout[IO] {
    override def getOrDefault(chatId: Long, botId: SBotId): IO[DBTimeoutData] =
      if botId == botIdI then db.get.map(
        _.find(t => t.chat_id == chatId).getOrElse(DBTimeoutData(Timeout(ChatId(chatId), botId)))
      )
      else IO.raiseError(new Throwable(s"Unexpected botId, actual: $botId - expected: $botIdI"))
    override def setTimeout(timeout: DBTimeoutData): IO[Unit] =
      db.update(ts => ts.filterNot(t => t.chat_id == timeout.chat_id) :+ timeout)
    override def removeTimeout(chatId: Long, botId: SBotId): IO[Unit] =
      db.update(ts => ts.filterNot(t => t.chat_id == chatId && t.bot_id == botId.value))
    override def logLastInteraction(chatId: Long, botId: SBotId): IO[Unit] =
      if botId == botIdI then db.update(ts =>
        ts.find(t => t.chat_id == chatId)
          .fold(ts)(oldValue =>
            ts.filterNot(_ == oldValue) :+ oldValue.copy(last_interaction = Instant.now().getEpochSecond().toString)
          )
      )
      else IO.raiseError(new Throwable(s"Unexpected botId, actual: $botId - expected: $botIdI"))
  }

  class DBMediaMock(db: Ref[IO, List[DBMediaData]]) extends DBMedia[IO] {
    override def getMedia(filename: String, cache: Boolean = true): IO[Option[DBMediaData]] =
      db.get.flatMap(
        _.find(m => m.media_name == filename)
          .fold[IO[Option[DBMediaData]]](None.pure)(data => data.some.pure)
      )
    override def getRandomMedia(botId: SBotId): IO[Option[DBMediaData]] =
      for {
        ls         <- db.get
        rnd        <- Random.scalaUtilRandom[IO]
        lsShuffled <- rnd.shuffleList(ls.filter(_.media_name.startsWith(botId.value)))
        result     <- lsShuffled.headOption.fold[IO[Option[DBMediaData]]](None.pure)(_.some.pure)
      } yield result
    override def getMediaByKind(kind: String, botId: SBotId, cache: Boolean = true): IO[List[DBMediaData]] =
      db.get.map(
        _.filter(m => m.kinds.contains(kind) && m.bot_id == botId.value)
      )
    override def getMediaByMediaCount(
        limit: Int = 20,
        botId: Option[SBotId] = None
    ): IO[List[DBMediaData]] =
      db.get.map(
        _.filter(m => botId.fold(true)(botId => m.media_name.startsWith(botId.value)))
          .sortBy(_.media_count)(using Ordering.Int.reverse)
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
    override def insertMedia(dbMediaData: DBMediaData): IO[Unit] =
      db.flatModify(ms =>
        ms.find(m => m.media_name == dbMediaData.media_name)
          .fold((dbMediaData :: ms, IO.unit))(oldValue =>
            (
              ms,
              IO.raiseError(
                Throwable(
                  s"[DBLayerMock] trying to insert $dbMediaData, but another one with the same media name exists: $oldValue"
                )
              )
            )
          )
      )
  }

  class DBSubscriptionMock(db: Ref[IO, List[DBSubscriptionData]]) extends DBSubscription[IO] {
    override def getSubscriptions(botId: SBotId, chatId: Option[Long] = None): IO[List[DBSubscriptionData]] =
      db.get.map(_.filter(subs => subs.bot_id == botId.value && chatId.fold(true)(_ == subs.chat_id)))
    override def insertSubscription(subscription: DBSubscriptionData): IO[Unit] =
      db.update((subs: List[DBSubscriptionData]) =>
        if subs.exists((s: DBSubscriptionData) => s.id == subscription.id) then throw new Throwable(
          "[TEST ERROR] Subscription id already present when inserting"
        )
        else subs :+ subscription
      )
    override def deleteSubscription(
        subscriptionId: UUID
    ): IO[Unit] =
      db.update((subs: List[DBSubscriptionData]) =>
        subs.filterNot((s: DBSubscriptionData) => s.id.equals(subscriptionId))
      )
    override def deleteSubscriptions(
        chatId: Long
    ): IO[Unit] =
      db.update((subs: List[DBSubscriptionData]) => subs.filterNot((s: DBSubscriptionData) => s.chat_id == chatId))
    override def getSubscription(id: String): IO[Option[DBSubscriptionData]] =
      db.get.map(_.find(sub => sub.id.toString == id))
    override def getRandomSubscription(): IO[Option[DBSubscriptionData]] =
      for {
        rnd  <- Random.scalaUtilRandom[IO]
        subs <- db.get
        elem <- rnd.elementOf(subs).map(Some(_)).handleError(_ => None)
      } yield elem
  }

  class DBShowMock(db: Ref[IO, List[DBShowData]]) extends DBShow[IO] {
    override def getShows(botId: SBotId): IO[List[DBShowData]] =
      db.get.map(_.filter(s => s.bot_id == botId.value))
    override def getRandomShow(botId: SBotId): IO[Option[DBShowData]] =
      for {
        rnd  <- Random.scalaUtilRandom[IO]
        subs <- db.get
        elem <- rnd.elementOf(subs.filter(_.bot_id == botId.value)).map(Some(_)).handleError(_ => None)
      } yield elem
    override def getShowByShowQuery(query: ShowQuery, botId: SBotId): IO[List[DBShowData]] =
      ???
    override def insertShow(dbShowData: DBShowData): IO[Unit] =
      for {
        shows <- db.get
        _     <- IO.raiseWhen(shows.exists(s => s.show_id == dbShowData.show_id))(
          Throwable(s"[DBShowMock] a show with id: ${dbShowData.show_id} already exists!")
        )
        _ <- db.update(shows => shows :+ dbShowData)
      } yield ()
    override def deleteShow(dbShowData: DBShowData): IO[Unit] = ???
  }

  class DBLogMock(db: Ref[IO, List[DBLogData]]) extends DBLog[IO] {
    override def writeLog(logMessage: String): IO[Unit] =
      db.update(ms => {
        val data = DBLogData(
          log_time = Instant.now().toEpochMilli,
          message = logMessage
        )
        ms :+ data
      })
    override def getLastLog(): IO[Option[DBLogData]] =
      db.get.map(_.lastOption)
  }
}
