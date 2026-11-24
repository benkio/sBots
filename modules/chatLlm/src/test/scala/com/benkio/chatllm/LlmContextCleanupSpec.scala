package com.benkio.chatllm

import cats.effect.IO
import cats.effect.Ref
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.Logger.given
import com.benkio.chatllm.llm.LlmContextConfig
import com.benkio.chatllm.llm.LlmTraceConfig
import com.benkio.chatllm.observability.LlmTrace
import com.benkio.chatllm.observability.LlmTraceStore
import com.benkio.chatllm.repository.db.LlmContextMessage
import com.benkio.chatllm.repository.db.LlmContextRole
import com.benkio.chatllm.repository.db.LlmContextStore
import munit.CatsEffectSuite

import scala.concurrent.duration.*

/** #1489: the TTL cleanup's cutoff, independently of the cron schedule. */
class LlmContextCleanupSpec extends CatsEffectSuite {

  test("the cleanup cuts off exactly at now minus the configured TTL") {
    for {
      cutoff <- Ref[IO].of(0L)
      store = new LlmContextStore[IO] {
        override def append(chatId: ChatId, botId: SBotId, role: LlmContextRole, content: String): IO[Unit] =
          IO.unit
        override def all(chatId: ChatId, botId: SBotId): IO[List[LlmContextMessage]] = IO.pure(Nil)
        override def recent(chatId: ChatId, botId: SBotId, limit: Int, notBefore: Long): IO[List[LlmContextMessage]] =
          IO.pure(Nil)
        override def deleteOlderThan(value: Long): IO[Int]                   = cutoff.set(value).as(3)
        override def delete(chatId: ChatId, botId: SBotId): IO[Int]          = IO.pure(0)
        override def trim(chatId: ChatId, botId: SBotId, keep: Int): IO[Int] = IO.pure(0)
      }
      deleted <- LlmContextCleanup.runAt[IO](store, LlmContextConfig(ttl = 2.hours), 10_000L)
      applied <- cutoff.get
    } yield {
      assertEquals(applied, 10_000L - 2.hours.toSeconds)
      assertEquals(deleted, 3)
    }
  }

  test("the trace cleanup still runs when the context cleanup fails, and the other way round") {
    def contextStore(fail: Boolean): LlmContextStore[IO] = new LlmContextStore[IO] {
      override def append(chatId: ChatId, botId: SBotId, role: LlmContextRole, content: String): IO[Unit] = IO.unit
      override def all(chatId: ChatId, botId: SBotId): IO[List[LlmContextMessage]]                        = IO.pure(Nil)
      override def recent(chatId: ChatId, botId: SBotId, limit: Int, notBefore: Long): IO[List[LlmContextMessage]] =
        IO.pure(Nil)
      override def deleteOlderThan(value: Long): IO[Int] =
        if fail then IO.raiseError(new RuntimeException("SQLITE_BUSY")) else IO.pure(1)
      override def delete(chatId: ChatId, botId: SBotId): IO[Int]          = IO.pure(0)
      override def trim(chatId: ChatId, botId: SBotId, keep: Int): IO[Int] = IO.pure(0)
    }
    def traceStore(cutoff: Ref[IO, Option[Long]], fail: Boolean): LlmTraceStore[IO] = new LlmTraceStore[IO] {
      override def append(trace: LlmTrace): IO[Unit]                        = IO.unit
      override def recent(chatHash: String, limit: Int): IO[List[LlmTrace]] = IO.pure(Nil)
      override def deleteOlderThan(value: Long): IO[Int]                    =
        if fail then IO.raiseError(new RuntimeException("SQLITE_BUSY")) else cutoff.set(Some(value)).as(2)
    }
    val trace = LlmTraceConfig(ttl = 1.hour)
    for {
      cutoff <- Ref[IO].of(Option.empty[Long])
      _      <- LlmContextCleanup.runAll[IO](
        contextStore(fail = true),
        LlmContextConfig(),
        traceStore(cutoff, fail = false),
        trace
      )
      afterContextFailure <- cutoff.get
      other               <- Ref[IO].of(Option.empty[Long])
      // a failing trace cleanup must not stop the context one either (and must not raise)
      _ <- LlmContextCleanup
        .runAll[IO](contextStore(fail = false), LlmContextConfig(), traceStore(other, fail = true), trace)
    } yield assert(afterContextFailure.isDefined, "traces must be deleted although the context cleanup failed")
  }
}
