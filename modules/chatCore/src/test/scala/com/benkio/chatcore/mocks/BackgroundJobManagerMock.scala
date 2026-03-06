package com.benkio.chatcore.mocks

import cats.effect.IO
import cats.syntax.all.*
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionId
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chatcore.SubscriptionKey
import fs2.concurrent.SignallingRef
import fs2.Stream
import log.effect.LogWriter

import java.time.Instant
import scala.collection.mutable.Map as MMap

object BackgroundJobManagerMock {
  def mock(): BackgroundJobManager[IO] = {
    val scheduleReferences: MMap[SubscriptionKey, SignallingRef[IO, Boolean]] = MMap.empty
    new BackgroundJobManagerMock(scheduleReferences)
  }

  class BackgroundJobManagerMock(
      scheduleReferences: MMap[SubscriptionKey, SignallingRef[IO, Boolean]]
  ) extends BackgroundJobManager[IO] {

    override def scheduleSubscription(subscription: Subscription): IO[Unit] =
      for {
        key <- IO.pure(SubscriptionKey(subscription.id, subscription.chatId))
        _   <- IO.raiseWhen(scheduleReferences.contains(key))(
          new RuntimeException(s"Subscription already exists: $subscription")
        )
        cancel <- SignallingRef[IO, Boolean](false)
        _      <- IO {
          scheduleReferences += key -> cancel
        }
      } yield ()

    override def getScheduledSubscriptions(): List[SubscriptionKey] =
      scheduleReferences.keys.toList

    override def cancelSubscription(subscriptionId: SubscriptionId): IO[Unit] =
      for {
        keyOpt <- IO(scheduleReferences.keys.find(_.subscriptionId == subscriptionId))
        _      <- keyOpt.fold(
          IO.raiseError(new RuntimeException(s"SubscriptionId not found: $subscriptionId"))
        ) { key =>
          for {
            cancel <- IO(scheduleReferences(key))
            _      <- cancel.set(true)
            _      <- IO {
              scheduleReferences -= key
            }
          } yield ()
        }
      } yield ()

    override def cancelSubscriptions(chatId: ChatId): IO[Unit] =
      for {
        keysToCancel <- IO(scheduleReferences.keys.filter(_.chatId == chatId).toList)
        _            <- keysToCancel.traverse_ { key =>
          for {
            cancel <- IO(scheduleReferences(key))
            _      <- cancel.set(true)
            _      <- IO {
              scheduleReferences -= key
            }
          } yield ()
        }
      } yield ()

    override def runSubscription(
        subscription: Subscription
    )(using log: LogWriter[IO]): IO[(Stream[IO, Instant], SignallingRef[IO, Boolean])] =
      for {
        cancel <- SignallingRef[IO, Boolean](false)
        action = cats.effect.IO.realTimeInstant
        stream = Stream.eval(action).interruptWhen(cancel)
      } yield (stream, cancel)
  }
}
