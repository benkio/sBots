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

  /** The longest provider error message kept for the logs: the body is untrusted and unbounded. */
  val MaxErrorChars: Int = 200

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
                Concurrent[F].pure(unreadable(body))
              case Right(json) =>
                // untrusted and unbounded: capped, so a log line and a panel grouped by model stay small
                val served = extractModel(json).map(_.trim.take(MaxModelChars)).filter(_.nonEmpty)
                extractText(json)
                  .fold(unreadable(body)) { raw =>
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
        failed(response, ProviderResponse.rateLimited)
      case status if status.code >= 500 && status.code < 600 =>
        failed(response, ProviderResponse.serverError)
      case _ =>
        failed(response, ProviderResponse.rejected)
    }

  /** A 200 whose body is not JSON or holds no model text: the capped body is kept as the failure message. */
  private def unreadable(body: String): ProviderResponse =
    ProviderResponse.malformedJson.copy(failure =
      Some(ProviderFailure(httpStatus = 200, message = Some(s"unreadable response: ${excerpt(body)}")))
    )

  /** Untrusted and unbounded text, on one line and capped, for the logs. */
  def excerpt(text: String): String = text.replaceAll("\\s+", " ").trim.take(MaxErrorChars)

  /** A non-2xx response: the result plus the status and the provider's own error message (`error.message`, the shape of
    * both Gemini and OpenRouter), read best effort and capped. The body holds no prompt or key.
    */
  private def failed[F[_]: Concurrent](response: Response[F], base: ProviderResponse): F[ProviderResponse] =
    response
      .as[String]
      .attempt
      .map { body =>
        val message = body.toOption
          .flatMap(parser.parse(_).toOption)
          .flatMap(_.hcursor.downField("error").get[String]("message").toOption)
          .map(excerpt)
          .filter(_.nonEmpty)
        base.copy(failure = Some(ProviderFailure(httpStatus = response.status.code, message = message)))
      }
}
