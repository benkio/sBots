package com.benkio.chatllm

import cats.effect.Async
import cats.effect.Fiber
import cats.syntax.all.*
import com.benkio.chatcore.CronBackgroundJobManager
import com.benkio.chatllm.llm.LlmContextConfig
import com.benkio.chatllm.repository.db.LlmContextStore
import cron4s.Cron
import doobie.util.transactor.Transactor
import log.effect.LogWriter

/** #1489: the TTL cleanup of `llm_context`. The job lives in chatLlm, but the cron scheduling is the one already used
  * by chatCore's `CronBackgroundJobManager`; `main` registers the job once at startup.
  */
object LlmContextCleanup {

  /** Starts the cleanup schedule and returns its fiber, so `main` can cancel it on shutdown. The cron and the TTL come
    * from `main.llm.context`.
    */
  def start[F[_]: Async](
      transactor: Transactor[F],
      config: LlmContextConfig
  )(using log: LogWriter[F]): F[Fiber[F, Throwable, Unit]] =
    Async[F]
      .fromEither(
        Cron(config.cleanupCron).left.map(e => new IllegalArgumentException(s"invalid llm.context.cleanup-cron: $e"))
      )
      .flatMap(cron => CronBackgroundJobManager.scheduleJob[F](cron, run(LlmContextStore[F](transactor), config).void))

  /** One cleanup: drops what is older than the TTL and reports how much was removed. */
  def run[F[_]: Async](store: LlmContextStore[F], config: LlmContextConfig)(using log: LogWriter[F]): F[Int] =
    Async[F].realTimeInstant.flatMap(now => runAt(store = store, config = config, nowEpochSecond = now.getEpochSecond))

  /** The same cleanup with an explicit clock, so the cutoff can be asserted in a test. */
  def runAt[F[_]: Async](store: LlmContextStore[F], config: LlmContextConfig, nowEpochSecond: Long)(using
      log: LogWriter[F]
  ): F[Int] = {
    val cutoff = nowEpochSecond - config.ttl.toSeconds
    store.deleteOlderThan(cutoff).flatMap { deleted =>
      log.info(s"[LlmContextCleanup] deleted $deleted context messages older than $cutoff").as(deleted)
    }
  }
}
