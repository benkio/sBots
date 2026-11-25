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

/** Gemini implementation of [[LlmProviderClientApi]], over the same http4s client used by the rest of the project. It
  * talks to the Interactions API (`POST /v1beta/interactions`), the successor of the legacy `generateContent`.
  *
  * Every request is a stateless one-shot: `store=false` (the API stores interactions by default) and no
  * `previous_interaction_id`, so the provider keeps nothing of the prompt beyond its normal abuse monitoring.
  */
object GeminiClient {

  /** The model asked when none is configured (`main.llm.gemini.model`): it now travels in the request body, not in the
    * endpoint.
    */
  val DefaultModel: String = "gemini-flash-lite-latest"

  def modelFor(connection: ProviderConnection): String =
    connection.model.map(_.trim).filter(_.nonEmpty).getOrElse(DefaultModel)

  /** The output contract asked of the model: `action` is required, `text` and `replyId` depend on the action. */
  private[llm] val responseSchema: Json = Json.obj(
    "type"       -> Json.fromString("object"),
    "properties" -> Json.obj(
      "text"   -> Json.obj("type" -> Json.fromString("string")),
      "action" -> Json.obj(
        "type" -> Json.fromString("string"),
        "enum" -> Json.arr(Json.fromString("text"), Json.fromString("reuse"), Json.fromString("silent"))
      ),
      "replyId" -> Json.obj("type" -> Json.fromString("string"))
    ),
    "required" -> Json.arr(Json.fromString("action"))
  )

  /** Why the interaction yielded no usable text: its `status` (`incomplete` is a reply cut by the output limit,
    * `failed` an error) and the output token count. Only these fields are read, never the content. Pure, so it is
    * tested on plain JSON.
    */
  private[llm] def diagnose(json: Json): Option[String] = {
    val cursor = json.hcursor
    val status = cursor.get[String]("status").toOption.map(f => s"status=$f")
    val tokens = cursor.downField("usage").get[Int]("total_output_tokens").toOption.map(t => s"output_tokens=$t")
    val parts  = List(status, tokens).flatten
    Option.when(parts.nonEmpty)(parts.mkString(" "))
  }

  /** The Interactions API reports the counts in `usage`: `total_input_tokens` (which includes the cached part),
    * `total_output_tokens`, `total_thought_tokens` (thinking models: billed as output but not in the output count, so
    * it is added to the completion count) and `total_cached_tokens`. Only these numbers are read, never the content.
    */
  private[llm] def usageOf(json: Json): Option[TokenUsage] = {
    val usage = json.hcursor.downField("usage")
    TokenUsage.of(
      prompt = usage.get[Int]("total_input_tokens").toOption,
      completion = {
        val counts = List("total_output_tokens", "total_thought_tokens").flatMap(usage.get[Int](_).toOption)
        Option.when(counts.nonEmpty)(counts.sum)
      },
      cached = usage.get[Int]("total_cached_tokens").toOption
    )
  }

  def apply[F[_]: Concurrent](
      client: Client[F],
      connection: ProviderConnection,
      maxOutputTokens: Int = LlmConfig.defaultMaxOutputTokens
  ): F[LlmProviderClientApi[F]] =
    ProviderHttp
      .parseUri[F](connection.endpoint, connection.apiKey.isDefined)
      .map(uri => new GeminiClientImpl[F](client, connection, uri, modelFor(connection), maxOutputTokens))

  private final class GeminiClientImpl[F[_]: Concurrent](
      client: Client[F],
      connection: ProviderConnection,
      uri: Uri,
      model: String,
      maxOutputTokens: Int
  ) extends LlmProviderClientApi[F] {

    override val provider: LlmProvider = LlmProvider.Gemini

    override def generate(value: String): F[ProviderResponse] =
      ProviderHttp.post(
        client,
        buildRequest(uri, value),
        extractText,
        extractModel,
        diagnose = diagnose,
        extractUsage = GeminiClient.usageOf
      )

    /** The reply text lives in the first `model_output` step of `steps`, as the `text` items of its `content`: thought
      * steps are skipped.
      */
    private val extractText: Json => Option[String] = json =>
      for {
        steps  <- json.asObject.flatMap(_("steps")).flatMap(_.asArray)
        output <- steps.find(_.hcursor.get[String]("type").contains("model_output"))
        items  <- output.asObject.flatMap(_("content")).flatMap(_.asArray)
        texts = items
          .filter(_.hcursor.get[String]("type").contains("text"))
          .flatMap(_.hcursor.get[String]("text").toOption)
        if texts.nonEmpty
      } yield texts.mkString

    /** The interaction reports the model that served the request at the top level `model`. */
    private val extractModel: Json => Option[String] = json => json.asObject.flatMap(_("model")).flatMap(_.asString)

    private def buildRequest(uri: Uri, value: String): Request[F] = {
      val body = Json.obj(
        "model"           -> Json.fromString(model),
        "input"           -> Json.fromString(value),
        "store"           -> Json.False,
        "response_format" -> Json.obj(
          "type"      -> Json.fromString("text"),
          "mime_type" -> Json.fromString("application/json"),
          "schema"    -> responseSchema
        ),
        "generation_config" -> Json.obj("max_output_tokens" -> Json.fromInt(maxOutputTokens))
      )

      val request = Request[F](method = Method.POST, uri = uri).withEntity(body)
      connection.apiKey.fold(request) { key =>
        request.putHeaders(Header.Raw(CIString("x-goog-api-key"), key.value))
      }
    }
  }
}
