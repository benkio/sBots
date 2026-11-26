package com.benkio.replieseditor.server.endpoints

import cats.effect.IO
import com.benkio.replieseditor.server.module.ApiError
import com.benkio.replieseditor.server.store.BotStoreApi
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

object RepliesChunkEndpoint {
  private object OffsetParam extends OptionalQueryParamDecoderMatcher[Int]("offset")
  private object LimitParam  extends OptionalQueryParamDecoderMatcher[Int]("limit")
}

final class RepliesChunkEndpoint(botStore: BotStoreApi) {
  import RepliesChunkEndpoint.*

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "api" / "bot" / botId / "replies-chunk" :? OffsetParam(
          offsetOpt
        ) +& LimitParam(limitOpt) =>
      val offset = offsetOpt.getOrElse(0)
      val limit  = limitOpt.getOrElse(50)
      botStore
        .getRepliesChunk(botId, offset, limit)
        .flatMap(
          _.fold(
            err =>
              if err.error.startsWith("Unknown botId") then NotFound(err.asJson)
              else InternalServerError(err.asJson),
            chunk => Ok(chunk.asJson)
          )
        )
  }
}
