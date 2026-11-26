package com.benkio.replieseditor.server.endpoints

import cats.effect.IO
import com.benkio.replieseditor.server.module.ApiError
import com.benkio.replieseditor.server.store.BotStoreApi
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

final class RepliesEndpoint(botStore: BotStoreApi) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] { case GET -> Root / "api" / "bot" / botId / "replies" =>
    botStore
      .getReplies(botId)
      .flatMap(
        _.fold(
          err =>
            if err.error.startsWith("Unknown botId") then NotFound(err.asJson)
            else InternalServerError(err.asJson),
          json => Ok(json)
        )
      )
  }
}
