package com.benkio.telegrambotinfrastructure

import cats.effect.Sync
import scalacache._
import scalacache.logging.Logger
import scalacache.memoization.MemoizationConfig

import scala.concurrent.duration.Duration

class EmptyCache[F[_], V](implicit val F: Sync[F], val config: MemoizationConfig) extends AbstractCache[F, String, V] {

  override protected def logger = Logger.getLogger("EmptyCache")

  override protected def doGet(key: String) =
    F.pure(None)

  override protected def doPut(key: String, value: V, ttl: Option[Duration]) =
    F.unit

  override protected def doRemove(key: String) =
    F.unit

  override protected val doRemoveAll =
    F.unit

  override val close = F.unit

}
