package com.benkio.replieseditor.server.endpoints

import cats.effect.IO
import cats.syntax.either.*
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.replieseditor.server.module.ApiError
import com.benkio.replieseditor.server.store.BotStoreApi
import io.circe.syntax.*
import io.circe.Json
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

final class SaveRepliesEndpoint(botStore: BotStoreApi) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] { case req @ POST -> Root / "api" / "bot" / botId / "replies" =>
    for {
      bodyJson <- req.as[Json]
      replies  <- IO.fromEither(
        bodyJson.as[List[ReplyBundleMessage]].leftMap(df => new RuntimeException(df.message))
      )
      res <- botStore
        .saveReplies(botId, replies)
        .flatMap(
          _.fold(
            err =>
              if err.error.startsWith("Unknown botId") then NotFound(err.asJson)
              else if err.error.startsWith("Media files not allowed") || err.error.startsWith("Cannot validate")
              then BadRequest(err.asJson)
              else InternalServerError(err.asJson),
            ok => Ok(ok.asJson)
          )
        )
    } yield res
  }
}
