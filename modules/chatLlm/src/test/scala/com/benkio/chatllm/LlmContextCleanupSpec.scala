package com.benkio.chatllm

import cats.effect.IO
import cats.effect.Ref
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.Logger.given
import com.benkio.chatllm.llm.LlmContextConfig
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
}
