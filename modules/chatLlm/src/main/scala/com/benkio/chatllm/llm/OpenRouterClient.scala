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

  /** The diagnosis of a response without usable text (see the client). Pure, so it is tested on plain JSON. */
  private[llm] def diagnose(json: Json): Option[String] = {
    val cursor  = json.hcursor
    val choice  = cursor.downField("choices").downN(0)
    val finish  = choice.get[String]("finish_reason").toOption.map(f => s"finish_reason=$f")
    val content = choice.downField("message").downField("content").focus.flatMap { c =>
      if c.isNull then Some("content=null")
      else c.asString.filter(_.isBlank).as("content=empty")
    }
    val choiceError = choice.downField("error").focus.map(errorText).map(e => s"choice_error=$e")
    val topError    = cursor.downField("error").focus.map(errorText).map(e => s"error=$e")
    val parts       = List(finish, content, choiceError, topError).flatten
    Option.when(parts.nonEmpty)(parts.mkString(" "))
  }

  private def errorText(error: Json): String = {
    val code    = error.hcursor.get[Int]("code").toOption.map(_.toString)
    val message = error.hcursor.get[String]("message").toOption
    List(code, message).flatten.mkString(" ").trim
  }

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

  /** OpenRouter reports the counts in `usage`: `prompt_tokens`, `completion_tokens` and, when the model caches,
    * `prompt_tokens_details.cached_tokens`. Only these numbers are read, never the content.
    */
  private[llm] def usageOf(json: Json): Option[TokenUsage] = {
    val usage = json.hcursor.downField("usage")
    TokenUsage.of(
      prompt = usage.get[Int]("prompt_tokens").toOption,
      completion = usage.get[Int]("completion_tokens").toOption,
      cached = usage.downField("prompt_tokens_details").get[Int]("cached_tokens").toOption
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
      ProviderHttp.post(client, buildRequest(uri, value), extractText, extractModel, diagnose, OpenRouterClient.usageOf)

    /** The reply text of OpenRouter lives at `choices[0].message.content`. */
    private val extractText: Json => Option[String] = json =>
      for {
        choices <- json.asObject.flatMap(_("choices")).flatMap(_.asArray)
        choice  <- choices.headOption
        message <- choice.asObject.flatMap(_("message"))
        content <- message.asObject.flatMap(_("content")).flatMap(_.asString)
      } yield content

    /** Why a reply carries no usable text, from the documented fields of the first choice: the normalized
      * `finish_reason` (`length` is a token limit hit, `content_filter` a block, `error` a provider failure), a null or
      * empty `message.content`, the choice's `error` object and a top-level `error`. Only these fields are read: never
      * the prompt or the content itself.
      */
    private[llm] val diagnose: Json => Option[String] = OpenRouterClient.diagnose

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
