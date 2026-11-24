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

/** OpenRouter implementation of [[LlmProviderClientApi]], over the same http4s client used by the rest of the project.
  */
object OpenRouterClient {

  /** The model asked when none is configured: OpenRouter's free router, which picks a free model per request. */
  val DefaultModel: String = "openrouter/free"

  /** The configured model (`main.llm.openrouter.model`, empty by default), or [[DefaultModel]] when it is empty or
    * blank. A configured model is the operator's choice: it can be a paid one.
    */
  def modelFor(connection: ProviderConnection): String =
    connection.model.map(_.trim).filter(_.nonEmpty).getOrElse(DefaultModel)

  /** Free models are the free router and the ids ending in `:free`. */
  def isFree(model: String): Boolean = model == DefaultModel || model.endsWith(":free")

  /** A configured model that is not free: it is allowed (it is the operator's choice) but it bills every fallback call,
    * and an old `OPENROUTER_MODEL` left in a deployment would do so unnoticed, so startup warns about it.
    */
  def paidModelWarning(connection: ProviderConnection): Option[String] = {
    val model = modelFor(connection)
    Option.when(!isFree(model))(
      s"[Main] llm: the OpenRouter model '$model' is not a free model (no ':free' suffix): every fallback call may be billed. " +
        "Leave openrouter.model / OPENROUTER_MODEL empty to use openrouter/free"
    )
  }

  def apply[F[_]: Concurrent](
      client: Client[F],
      connection: ProviderConnection,
      maxOutputTokens: Int = LlmConfig.defaultMaxOutputTokens
  ): F[LlmProviderClientApi[F]] =
    ProviderHttp
      .parseUri[F](connection.endpoint, connection.apiKey.isDefined)
      .map(uri => new OpenRouterClientImpl[F](client, connection, uri, modelFor(connection), maxOutputTokens))

  private final class OpenRouterClientImpl[F[_]: Concurrent](
      client: Client[F],
      connection: ProviderConnection,
      uri: Uri,
      model: String,
      maxOutputTokens: Int
  ) extends LlmProviderClientApi[F] {

    override val provider: LlmProvider = LlmProvider.OpenRouter

    override def generate(value: String): F[ProviderResponse] =
      ProviderHttp.post(client, buildRequest(uri, value), extractText, extractModel)

    /** The reply text of OpenRouter lives at `choices[0].message.content`. */
    private val extractText: Json => Option[String] = json =>
      for {
        choices <- json.asObject.flatMap(_("choices")).flatMap(_.asArray)
        choice  <- choices.headOption
        message <- choice.asObject.flatMap(_("message"))
        content <- message.asObject.flatMap(_("content")).flatMap(_.asString)
      } yield content

    /** OpenRouter reports the model that served the request at the top level `model`: with the free router it is the
      * one the router picked.
      */
    private val extractModel: Json => Option[String] = json => json.asObject.flatMap(_("model")).flatMap(_.asString)

    private def buildRequest(uri: Uri, value: String): Request[F] = {
      val body = Json.obj(
        "model"    -> Json.fromString(model),
        "messages" -> Json.arr(
          Json.obj("role" -> Json.fromString("user"), "content" -> Json.fromString(value))
        ),
        "response_format" -> Json.obj("type" -> Json.fromString("json_object")),
        "max_tokens"      -> Json.fromInt(maxOutputTokens)
      )

      val request = Request[F](method = Method.POST, uri = uri).withEntity(body)
      connection.apiKey.fold(request) { key =>
        request.putHeaders(Header.Raw(CIString("Authorization"), s"Bearer ${key.value}"))
      }
    }
  }
}
