package com.benkio.replieseditor.server.endpoints

import cats.effect.IO
import cats.syntax.either.*
import com.benkio.replieseditor.server.module.FilterReq
import com.benkio.replieseditor.server.store.BotStoreApi
import io.circe.syntax.*
import io.circe.Json
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

object FilteredRepliesChunkEndpoint {
  private object OffsetParam extends OptionalQueryParamDecoderMatcher[Int]("offset")
  private object LimitParam  extends OptionalQueryParamDecoderMatcher[Int]("limit")
}

final class FilteredRepliesChunkEndpoint(botStore: BotStoreApi) {
  import FilteredRepliesChunkEndpoint.*

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "api" / "bot" / botId / "replies-filter-chunk" :? OffsetParam(offsetOpt) +& LimitParam(
          limitOpt
        ) =>
      val offset = offsetOpt.getOrElse(0)
      val limit  = limitOpt.getOrElse(50)
      for {
        bodyJson <- req.as[Json]
        filter   <- IO.fromEither(bodyJson.as[FilterReq].leftMap(df => new RuntimeException(df.message)))
        res      <- botStore
          .getFilteredRepliesChunk(botId, filter.message, offset, limit)
          .flatMap(
            _.fold(
              err =>
                if err.error.startsWith("Unknown botId") then NotFound(err.asJson)
                else InternalServerError(err.asJson),
              chunk => Ok(chunk.asJson)
            )
          )
      } yield res
  }
}
