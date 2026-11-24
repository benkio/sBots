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

  /** The longest model name kept from a provider response. */
  val MaxModelChars: Int = 100

  def parseUri[F[_]: Concurrent](endpoint: String, hasApiKey: Boolean): F[Uri] =
    Concurrent[F].fromEither(
      Uri
        .fromString(endpoint)
        .leftMap(err => new IllegalArgumentException(err.message))
        .flatMap { uri =>
          if hasApiKey && !uri.scheme.contains(Uri.Scheme.https) then Left(
            new IllegalArgumentException("Refusing to send API key over non-https endpoint")
          )
          else Right(uri)
        }
    )

  /** Shared mapping of the HTTP status and of the nested JSON payload to the provider-independent result. */
  def post[F[_]: Concurrent](
      client: Client[F],
      request: Request[F],
      extractText: Json => Option[String],
      extractModel: Json => Option[String] = _ => None
  ): F[ProviderResponse] =
    client
      .run(request)
      .use(response => fromResponse(response = response, extractText = extractText, extractModel = extractModel))

  private def fromResponse[F[_]: Concurrent](
      response: Response[F],
      extractText: Json => Option[String],
      extractModel: Json => Option[String]
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
                // untrusted and unbounded: capped, so a log line and a panel grouped by model stay small
                val served = extractModel(json).map(_.trim.take(MaxModelChars)).filter(_.nonEmpty)
                extractText(json)
                  .fold(ProviderResponse.malformedJson) { raw =>
                    StructuredReply
                      .parseDetailed(raw)
                      .fold(
                        _ => ProviderResponse.malformedJson.copy(raw = Some(raw), model = served),
                        (reply, stripped) =>
                          ProviderResponse.success(reply).copy(raw = Some(raw), stripped = stripped, model = served)
                      )
                  }
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
