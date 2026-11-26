package com.benkio.replieseditor.server.endpoints

import cats.effect.IO
import com.benkio.replieseditor.server.store.BotStoreApi
import io.circe.syntax.*
import io.circe.Json
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

final class ReloadBotEndpoint(botStore: BotStoreApi) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] { case POST -> Root / "api" / "bot" / botId / "reload" =>
    botStore
      .reloadBotFromDisk(botId)
      .flatMap(
        _.fold(
          err =>
            if err.error.startsWith("Unknown botId") then NotFound(err.asJson)
            else InternalServerError(err.asJson),
          _ => Ok(Json.obj("ok" -> Json.fromBoolean(true), "botId" -> Json.fromString(botId)))
        )
      )
  }
}
