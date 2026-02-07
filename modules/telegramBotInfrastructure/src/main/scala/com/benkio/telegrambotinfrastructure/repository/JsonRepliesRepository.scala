package com.benkio.main

import cats.effect.Async
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import log.effect.LogWriter

trait JsonRepliesRepository[F[_]] {
  def loadReplies(sBotConfig: SBotConfig): F[List[ReplyBundleMessage]]
}

object JsonRepliesRepository {
  def apply[F[_]: Async: LogWriter](): JsonRepliesRepository[F] = new JsonRepliesRepositoryImpl[F]();

  class JsonRepliesRepositoryImpl[F[_]: Async: LogWriter]() extends JsonRepliesRepository[F] {
    // TODO: Implement - given the path to the json file. load it's ReplyBundleMessages
    override def loadReplies(sBotConfig: SBotConfig): F[List[ReplyBundleMessage]] = ???
  }
}
