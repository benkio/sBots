package com.benkio.chatllm.llm

import cats.effect.Concurrent
import cats.syntax.all.*
import io.circe.Json
import org.http4s.circe.*
import org.http4s.client.Client
import org.http4s.Header
import org.http4s.Method
import org.http4s.Request
import org.http4s.Uri
import org.typelevel.ci.CIString

/** Gemini implementation of [[LlmProviderClientApi]], over the same http4s client used by the rest of the project. */
object GeminiClient {

  def apply[F[_]: Concurrent](client: Client[F], connection: ProviderConnection): F[LlmProviderClientApi[F]] =
    ProviderHttp.parseUri[F](connection.endpoint).map(uri => new GeminiClientImpl[F](client, connection, uri))

  private final class GeminiClientImpl[F[_]: Concurrent](
      client: Client[F],
      connection: ProviderConnection,
      uri: Uri
  ) extends LlmProviderClientApi[F] {

    override val provider: LlmProvider = LlmProvider.Gemini

    override def generate(value: String): F[ProviderResponse] =
      ProviderHttp.post(client, buildRequest(uri, value), extractText)

    /** The reply text of Gemini lives at `candidates[0].content.parts[0].text`. */
    private val extractText: Json => Option[String] = json =>
      for {
        candidates <- json.asObject.flatMap(_("candidates")).flatMap(_.asArray)
        candidate  <- candidates.headOption
        content    <- candidate.asObject.flatMap(_("content"))
        parts      <- content.asObject.flatMap(_("parts")).flatMap(_.asArray)
        part       <- parts.headOption
        text       <- part.asObject.flatMap(_("text")).flatMap(_.asString)
      } yield text

    private def buildRequest(uri: Uri, value: String): Request[F] = {
      val body = Json.obj(
        "contents" -> Json.arr(
          Json.obj(
            "role"  -> Json.fromString("user"),
            "parts" -> Json.arr(Json.obj("text" -> Json.fromString(value)))
          )
        ),
        "generationConfig" -> Json.obj(
          "responseMimeType" -> Json.fromString("application/json"),
          "responseSchema"   -> Json.obj(
            "type"       -> Json.fromString("OBJECT"),
            "properties" -> Json.obj("text" -> Json.obj("type" -> Json.fromString("STRING"))),
            "required"   -> Json.arr(Json.fromString("text"))
          )
        )
      )

      val request = Request[F](method = Method.POST, uri = uri).withEntity(body)
      connection.apiKey.fold(request) { key =>
        request.putHeaders(Header.Raw(CIString("x-goog-api-key"), key.value))
      }
    }
  }
}
