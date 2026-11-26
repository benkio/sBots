package com.benkio.replieseditor.server.endpoints

import cats.effect.IO
import com.benkio.replieseditor.server.store.BotStoreApi
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

final class CommitRepliesEndpoint(botStore: BotStoreApi) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] { case POST -> Root / "api" / "bot" / botId / "replies" / "commit" =>
    botStore
      .commit(botId)
      .flatMap(
        _.fold(
          err =>
            if err.error.startsWith("Unknown botId") then NotFound(err.asJson)
            else if err.error.startsWith("Some media files are not present") then BadRequest(err.asJson)
            else InternalServerError(err.asJson),
          ok => Ok(ok.asJson)
        )
      )
  }
}
