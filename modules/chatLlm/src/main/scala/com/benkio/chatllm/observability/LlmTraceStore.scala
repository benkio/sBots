package com.benkio.chatllm.observability

import cats.effect.Async
import cats.syntax.all.*
import doobie.*
import doobie.implicits.*
import doobie.util.transactor.Transactor

/** One row of `llm_trace`: the full prompt and raw response of a completion, joined to its `[llm]` log line by the chat
  * hash, the bot and the time.
  */
final case class LlmTrace(
    createdAt: Long,
    chatHash: String,
    botId: String,
    promptVersion: String,
    model: Option[String],
    status: String,
    action: String,
    prompt: String,
    response: Option[String]
)

/** The bounded debug trace (#1493): written only when `llm.trace.enabled` is on, dropped by the TTL cleanup. */
trait LlmTraceStore[F[_]] {
  def append(trace: LlmTrace): F[Unit]

  /** The newest traces of a chat hash (all bots), newest first. Used by the tests and for ad-hoc inspection. */
  def recent(chatHash: String, limit: Int): F[List[LlmTrace]]

  /** Drops every trace older than the cutoff (epoch second), giving back how many were removed. */
  def deleteOlderThan(cutoff: Long): F[Int]
}

object LlmTraceStore {

  def apply[F[_]: Async](transactor: Transactor[F]): LlmTraceStore[F] =
    new LlmTraceStore[F] {
      override def append(trace: LlmTrace): F[Unit] = appendQuery(trace).run.transact(transactor).void

      override def recent(chatHash: String, limit: Int): F[List[LlmTrace]] =
        Async[F].raiseWhen(limit < 0)(new IllegalArgumentException("the trace limit must not be negative")) *>
          recentQuery(chatHash, limit).to[List].transact(transactor)

      override def deleteOlderThan(cutoff: Long): F[Int] = deleteOlderThanQuery(cutoff).run.transact(transactor)
    }

  def appendQuery(t: LlmTrace): Update0 =
    sql"""INSERT INTO llm_trace (created_at, chat_hash, bot_id, prompt_version, model, status, action, prompt, response)
          VALUES (${t.createdAt}, ${t.chatHash}, ${t.botId}, ${t.promptVersion}, ${t.model}, ${t.status}, ${t.action},
                  ${t.prompt}, ${t.response})""".update

  def recentQuery(chatHash: String, limit: Int): Query0[LlmTrace] =
    sql"""SELECT created_at, chat_hash, bot_id, prompt_version, model, status, action, prompt, response
          FROM llm_trace WHERE chat_hash = $chatHash
          ORDER BY created_at DESC, trace_id DESC LIMIT $limit""".query[LlmTrace]

  def deleteOlderThanQuery(cutoff: Long): Update0 =
    sql"""DELETE FROM llm_trace WHERE created_at < $cutoff""".update
}
