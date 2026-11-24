package com.benkio.chatllm.llm

/** The provider to use for a completion request. */
enum LlmProvider {
  case Gemini, OpenRouter

  override def toString: String = this match {
    case Gemini     => "gemini"
    case OpenRouter => "openrouter"
  }
}

object LlmProvider {

  /** The config keeps the provider as a string: this maps it to the type safe value, with `gemini` as the default (it
    * keeps the current behaviour when the new config is missing).
    */
  def fromString(value: Option[String]): Either[String, LlmProvider] =
    value.map(_.trim.toLowerCase).filter(_.nonEmpty).getOrElse("gemini") match {
      case "gemini"     => Right(LlmProvider.Gemini)
      case "openrouter" => Right(LlmProvider.OpenRouter)
      case other        => Left(s"Unsupported LLM primary provider: $other")
    }
}

/** The structured reply of a provider. `action` is required on the wire; `text` and `replyId` are the optional fields
  * of the output contract of the prompt builder (#1491); `text` is empty (set by `StructuredReply` when the field is
  * absent) for the other actions.
  */
final case class LlmReply(text: String, action: LlmAction, replyId: Option[String] = None)

/** The `action` of the output contract. A value outside these is rejected when the provider reply is parsed. */
enum LlmAction {
  case Text, Reuse, Silent
}

object LlmAction {
  def parse(raw: String): Option[LlmAction] = raw.trim.toLowerCase match {
    case "text"   => Some(Text)
    case "reuse"  => Some(Reuse)
    case "silent" => Some(Silent)
    case _        => None
  }
}

/** The outcome of one provider call. These are all singleton cases, so they can be compared and enumerated. A timeout
  * is a result rather than an exception so the attempts that led to it are kept in the completion.
  */
enum ProviderResult {
  case Success, RateLimited, ServerError, Timeout, Rejected, MalformedJson
}

/** What a provider boundary returned: a result, and the reply only when the result is a success. `raw` is the text the
  * model produced (kept also when it was malformed, to trace a weird reply) and `stripped` tells that the reply parsed
  * only after the fence/prose around the JSON object was cut away, and `model` is the model the provider says it used
  * (with `openrouter/free` the router picks one per request), when the response carries it (#1493).
  */
final case class ProviderResponse(
    result: ProviderResult,
    reply: Option[LlmReply],
    raw: Option[String] = None,
    stripped: Boolean = false,
    model: Option[String] = None,
    failure: Option[ProviderFailure] = None
)

/** Why a provider call failed over HTTP: the status code and the (capped) error message of the response body, so a
  * retired model or a revoked key is visible in the logs instead of only as a `rejected` label.
  */
final case class ProviderFailure(httpStatus: Int, message: Option[String])

object ProviderResponse {
  val rateLimited: ProviderResponse              = ProviderResponse(ProviderResult.RateLimited, None)
  val serverError: ProviderResponse              = ProviderResponse(ProviderResult.ServerError, None)
  val timeout: ProviderResponse                  = ProviderResponse(ProviderResult.Timeout, None)
  val rejected: ProviderResponse                 = ProviderResponse(ProviderResult.Rejected, None)
  val malformedJson: ProviderResponse            = ProviderResponse(ProviderResult.MalformedJson, None)
  def success(reply: LlmReply): ProviderResponse = ProviderResponse(ProviderResult.Success, Some(reply))
}

/** One provider attempt of a completion. The result is `None` while the provider has not answered yet. */
final case class ProviderAttempt(
    provider: LlmProvider,
    result: Option[ProviderResult],
    failure: Option[ProviderFailure] = None
)

/** The lifecycle of a completion. `Answered`, `Failed` and `Silent` are terminal: `Silent` is a completion that was
  * stopped by the local quota before any provider was asked, so no reply is sent.
  */
enum CompletionStatus {
  case Pending, Answered, Failed, Silent
}

/** The last raw model text of a completion, with the provider that produced it (so a fallback that fails without text
  * never takes the blame for, or the credit of, the primary's text) and whether it needed stripping.
  */
final case class RawResponse(
    provider: LlmProvider,
    text: String,
    stripped: Boolean,
    model: Option[String] = None
)

/** Why a prompt was refused before any provider was asked. */
enum LocalRejection(val label: String) {
  case OversizedPrompt    extends LocalRejection("oversized_prompt")
  case MissingCredentials extends LocalRejection("missing_credentials")
}

/** One completion per client call. The fallback is always the other provider: a config change switches both. */
final case class Completion(
    prompt: String,
    primaryProvider: LlmProvider,
    status: CompletionStatus,
    reply: Option[LlmReply],
    attempts: List[ProviderAttempt],
    // Observability only (#1493): the last raw model text, whether it needed fence/prose stripping, and why no
    // provider was asked when that was decided locally.
    raw: Option[RawResponse] = None,
    localRejection: Option[LocalRejection] = None
) {
  def fallbackProvider: LlmProvider = primaryProvider match {
    case LlmProvider.Gemini     => LlmProvider.OpenRouter
    case LlmProvider.OpenRouter => LlmProvider.Gemini
  }
}

object Completion {

  def started(prompt: String, primaryProvider: LlmProvider): Completion =
    startedWith(prompt = prompt, primaryProvider = primaryProvider, firstProvider = primaryProvider)

  /** A completion whose first actual call goes to `firstProvider`. When the primary's quota is exhausted the fallback
    * is asked first, so the completion carries only the fallback attempt while keeping the configured primary label.
    */
  def startedWith(prompt: String, primaryProvider: LlmProvider, firstProvider: LlmProvider): Completion =
    Completion(
      prompt = prompt,
      primaryProvider = primaryProvider,
      status = CompletionStatus.Pending,
      reply = None,
      attempts = List(ProviderAttempt(firstProvider, None))
    )

  /** No provider was asked: both providers' quotas are exhausted, the prompt exceeds the input limit or credentials are
    * incomplete. A provider timeout is not built here: it is recorded as a provider result instead.
    */
  def notRequested(prompt: String, primaryProvider: LlmProvider, status: CompletionStatus): Completion =
    Completion(
      prompt = prompt,
      primaryProvider = primaryProvider,
      status = status,
      reply = None,
      attempts = Nil
    )

  /** The primary failed retryably but the fallback's quota is exhausted: drop the unresolved fallback attempt so only
    * providers that were actually called are recorded, and fail the completion.
    */
  def failUnresolved(completion: Completion): Completion =
    completion.copy(
      status = CompletionStatus.Failed,
      reply = None,
      attempts = completion.attempts.filter(_.result.isDefined)
    )

  /** Applies the rules of `llm.allium` to a provider response. Responses for a terminal completion are ignored.
    *
    * A rate limit, server error, timeout, malformed reply or rejection (a revoked key, a removed model) is retried only
    * when it comes from the primary provider and the fallback has not been attempted yet: this creates the fallback
    * attempt and keeps the completion pending. The same result from the fallback (or any other non-retryable result)
    * fails the completion.
    */
  def acceptResponse(
      completion: Completion,
      provider: LlmProvider,
      response: ProviderResponse
  ): Completion =
    if completion.status != CompletionStatus.Pending then completion
    else if !completion.attempts.exists(_.provider == provider) then completion
    else {
      val traced = response.raw.fold(completion)(text =>
        completion.copy(raw = Some(RawResponse(provider, text, response.stripped, response.model)))
      )
      val withResult = traced.attempts.map { attempt =>
        if attempt.provider == provider then attempt.copy(result = Some(response.result), failure = response.failure)
        else attempt
      }
      response.result match {
        case ProviderResult.Success if response.reply.nonEmpty =>
          traced.copy(
            status = CompletionStatus.Answered,
            reply = response.reply,
            attempts = withResult
          )
        case ProviderResult.Success =>
          // A success without a reply is not a structured reply: it fails the completion.
          traced.copy(status = CompletionStatus.Failed, attempts = withResult)
        case ProviderResult.RateLimited | ProviderResult.ServerError | ProviderResult.Timeout |
            ProviderResult.MalformedJson | ProviderResult.Rejected =>
          val providerHasAttempt = withResult.exists(_.provider == provider)
          if provider == completion.primaryProvider && !withResult.exists(_.provider == completion.fallbackProvider)
          then
          // The primary was rate limited or failed: create the one allowed fallback attempt.
          traced.copy(
            attempts = withResult :+ ProviderAttempt(completion.fallbackProvider, None)
          )
          else if provider != completion.primaryProvider && !providerHasAttempt then
          // An unsolicited response from a provider that was never asked: no rule applies, keep the completion.
          completion
          else
            // The fallback was asked and failed too: the completion is exhausted.
            traced.copy(status = CompletionStatus.Failed, attempts = withResult)
      }
    }
}
