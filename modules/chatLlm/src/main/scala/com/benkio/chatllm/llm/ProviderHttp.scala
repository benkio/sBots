package com.benkio.chatllm.llm

import cats.effect.Concurrent
import cats.syntax.all.*
import io.circe.parser
import io.circe.Json
import org.http4s.client.Client
import org.http4s.Request
import org.http4s.Response
import org.http4s.Status
import org.http4s.Uri

private[llm] object ProviderHttp {

  def parseUri[F[_]: Concurrent](endpoint: String, hasApiKey: Boolean): F[Uri] =
    Concurrent[F].fromEither(
      Uri
        .fromString(endpoint)
        .leftMap(err => new IllegalArgumentException(err.message))
        .flatMap { uri =>
          if uri.scheme.contains(Uri.Scheme.http) && hasApiKey then Left(
            new IllegalArgumentException("Refusing to send API key over http endpoint")
          )
          else Right(uri)
        }
    )

  /** Shared mapping of the HTTP status and of the nested JSON payload to the provider-independent result. */
  def post[F[_]: Concurrent](
      client: Client[F],
      request: Request[F],
      extractText: Json => Option[String]
  ): F[ProviderResponse] =
    client.run(request).use(response => fromResponse(response, extractText))

  private def fromResponse[F[_]: Concurrent](
      response: Response[F],
      extractText: Json => Option[String]
  ): F[ProviderResponse] =
    response.status match {
      case Status.Ok =>
        response.as[String].attempt.flatMap {
          case Left(_) =>
            Concurrent[F].pure(ProviderResponse.malformedJson)
          case Right(body) =>
            parser.parse(body) match {
              case Left(_) =>
                Concurrent[F].pure(ProviderResponse.malformedJson)
              case Right(json) =>
                extractText(json)
                  .flatMap(raw => StructuredReply.parse(raw).toOption)
                  .fold(ProviderResponse.malformedJson)(ProviderResponse.success)
                  .pure[F]
            }
        }
      case Status.TooManyRequests =>
        Concurrent[F].pure(ProviderResponse.rateLimited)
      case status if status.code >= 500 && status.code < 600 =>
        Concurrent[F].pure(ProviderResponse.serverError)
      case _ =>
        Concurrent[F].pure(ProviderResponse.rejected)
    }
}
