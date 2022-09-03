package com.benkio.telegrambotinfrastructure

import cats.effect.Sync
import scalacache._
import scalacache.logging.Logger
import scalacache.memoization.MemoizationConfig

import scala.concurrent.duration.Duration

class EmptyCache[F[_], V](implicit val F: Sync[F], val config: MemoizationConfig) extends AbstractCache[F, String, V] {

  protected override def logger = Logger.getLogger("EmptyCache")

  protected override def doGet(key: String) =
    F.pure(None)

  protected override def doPut(key: String, value: V, ttl: Option[Duration]) =
    F.unit

  protected override def doRemove(key: String) =
    F.unit

  protected override val doRemoveAll =
    F.unit

  override val close = F.unit

}
