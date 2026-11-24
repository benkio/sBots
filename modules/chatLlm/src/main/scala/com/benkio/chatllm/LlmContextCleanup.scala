package com.benkio.chatllm

import cats.effect.Async
import cats.effect.Fiber
import cats.syntax.all.*
import com.benkio.chatcore.CronBackgroundJobManager
import com.benkio.chatllm.llm.LlmContextConfig
import com.benkio.chatllm.llm.LlmTraceConfig
import com.benkio.chatllm.observability.LlmTraceStore
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
      config: LlmContextConfig,
      trace: LlmTraceConfig = LlmTraceConfig()
  )(using log: LogWriter[F]): F[Fiber[F, Throwable, Unit]] =
    Async[F]
      .fromEither(
        Cron(config.cleanupCron).left.map(e => new IllegalArgumentException(s"invalid llm.context.cleanup-cron: $e"))
      )
      .flatMap(cron =>
        CronBackgroundJobManager.scheduleJob[F](
          cron,
          runAll(
            store = LlmContextStore[F](transactor),
            config = config,
            traces = LlmTraceStore[F](transactor),
            trace = trace
          )
        )
      )

  /** The two cleanups are independent: a failing context cleanup (a busy database, say) must not leave the traces,
    * which hold prompts, past their TTL, and the other way round. Each failure is logged, not propagated.
    */
  def runAll[F[_]: Async](
      store: LlmContextStore[F],
      config: LlmContextConfig,
      traces: LlmTraceStore[F],
      trace: LlmTraceConfig
  )(using log: LogWriter[F]): F[Unit] =
    run(store, config).void.handleErrorWith(e =>
      log.error(s"[LlmContextCleanup] context cleanup failed: ${e.getMessage}")
    ) *>
      runTraces(traces, trace).void.handleErrorWith(e =>
        log.error(s"[LlmContextCleanup] trace cleanup failed: ${e.getMessage}")
      )

  /** One cleanup: drops what is older than the TTL and reports how much was removed. */
  def run[F[_]: Async](store: LlmContextStore[F], config: LlmContextConfig)(using log: LogWriter[F]): F[Int] =
    Async[F].realTimeInstant.flatMap(now => runAt(store = store, config = config, nowEpochSecond = now.getEpochSecond))

  /** The same cleanup with an explicit clock, so the cutoff can be asserted in a test. */
  def runAt[F[_]: Async](store: LlmContextStore[F], config: LlmContextConfig, nowEpochSecond: Long)(using
      log: LogWriter[F]
  ): F[Int] = {
    val cutoff = config.cutoff(nowEpochSecond)
    store.deleteOlderThan(cutoff).flatMap { deleted =>
      log.info(s"[LlmContextCleanup] deleted $deleted context messages older than $cutoff").as(deleted)
    }
  }

  /** #1493: the debug traces expire on their own, shorter TTL, also when the trace has been turned off since. */
  def runTraces[F[_]: Async](store: LlmTraceStore[F], config: LlmTraceConfig)(using log: LogWriter[F]): F[Int] =
    Async[F].realTimeInstant.flatMap { now =>
      val cutoff = config.cutoff(now.getEpochSecond)
      store.deleteOlderThan(cutoff).flatMap { deleted =>
        log.info(s"[LlmContextCleanup] deleted $deleted LLM traces older than $cutoff").as(deleted)
      }
    }
}
