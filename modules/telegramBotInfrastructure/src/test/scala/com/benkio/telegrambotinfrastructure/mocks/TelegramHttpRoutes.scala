package com.benkio.telegrambotinfrastructure.mocks

import cats.syntax.all.*
import cats.effect.*
import org.http4s.*
import org.http4s.dsl.io.*

object TelegramHttpRoutes {
  def apply[F[_]: Async](expectedToken: String): HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root / s"bot$expectedToken" / "setWebhook" => Response(status = Status.Ok).pure[F]
  }
}
