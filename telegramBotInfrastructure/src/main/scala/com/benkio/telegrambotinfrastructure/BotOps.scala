package com.benkio.telegrambotinfrastructure

import cats._
import cats.effect.{Async, Resource}
import org.http4s._
import org.http4s.client.Client

// Contains general operations that are bot independent
trait BotOps {
  def token[F[_]: Async]: Resource[F, String]

  def deleteWebhooks[F[_]: Async](
      httpClient: Client[F],
      token: String,
  ): Resource[F, Response[F]] = for {
    uri <- Resource.eval(
      MonadThrow[F].fromEither(Uri.fromString(s"https://api.telegram.org/bot$token/setWebhook?url="))
    )
    deleteWebhookRequest: Request[F] =
      Request[F](
        method = Method.POST,
        uri = uri
      )
    response <- httpClient.run(deleteWebhookRequest)
  } yield response
}
