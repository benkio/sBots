package com.benkio.replieseditor.server.endpoints

import cats.effect.IO
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
      replies  <- bodyJson.as[List[ReplyBundleMessage]] match {
        case Left(df)  => IO.raiseError(new RuntimeException(df.message))
        case Right(xs) => IO.pure(xs)
      }
      res <- botStore.saveReplies(botId, replies).flatMap {
        case Left(err) =>
          if err.error.startsWith("Unknown botId") then NotFound(err.asJson)
          else if err.error.startsWith("Media files not allowed") || err.error.startsWith("Cannot validate")
          then BadRequest(err.asJson)
          else InternalServerError(err.asJson)
        case Right(ok) =>
          Ok(ok.asJson)
      }
    } yield res
  }
}
