package com.benkio.replieseditor.server.endpoints

import cats.effect.IO
import cats.syntax.either.*
import com.benkio.replieseditor.server.module.ApiError
import com.benkio.replieseditor.server.module.UpdateReplyReq
import com.benkio.replieseditor.server.store.BotStoreApi
import io.circe.syntax.*
import io.circe.Json
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

final class UpdateReplyEndpoint(botStore: BotStoreApi) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "api" / "bot" / botId / "replies" / "update" =>
      for {
        bodyJson <- req.as[Json]
        upd      <- IO.fromEither(bodyJson.as[UpdateReplyReq].leftMap(df => new RuntimeException(df.message)))
        res      <- botStore
          .updateReplyAt(botId, upd.index, upd.value)
          .flatMap(
            _.fold(
              err =>
                if err.error.startsWith("Unknown botId") then NotFound(err.asJson)
                else BadRequest(err.asJson),
              _ => Ok(Json.obj("ok" -> Json.fromBoolean(true)).asJson)
            )
          )
      } yield res
  }
}
