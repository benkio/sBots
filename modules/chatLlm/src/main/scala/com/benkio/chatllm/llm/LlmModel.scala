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

/** The structured reply of a provider: only the text is consumed so far. */
final case class LlmReply(text: String)

/** The outcome of one provider call. These are all singleton cases, so they can be compared and enumerated. */
enum ProviderResult {
  case Success, RateLimited, ServerError, Rejected, MalformedJson
}

/** What a provider boundary returned: a result, and the reply only when the result is a success. */
final case class ProviderResponse(result: ProviderResult, reply: Option[LlmReply])

object ProviderResponse {
  val rateLimited: ProviderResponse              = ProviderResponse(ProviderResult.RateLimited, None)
  val serverError: ProviderResponse              = ProviderResponse(ProviderResult.ServerError, None)
  val rejected: ProviderResponse                 = ProviderResponse(ProviderResult.Rejected, None)
  val malformedJson: ProviderResponse            = ProviderResponse(ProviderResult.MalformedJson, None)
  def success(reply: LlmReply): ProviderResponse = ProviderResponse(ProviderResult.Success, Some(reply))
}

/** One provider attempt of a completion. The result is `None` while the provider has not answered yet. */
final case class ProviderAttempt(provider: LlmProvider, result: Option[ProviderResult])

/** The lifecycle of a completion. `Answered` and `Failed` are terminal. */
enum CompletionStatus {
  case Pending, Answered, Failed
}

/** One completion per client call. The fallback is always the other provider: a config change switches both. */
final case class Completion(
    prompt: String,
    primaryProvider: LlmProvider,
    status: CompletionStatus,
    reply: Option[LlmReply],
    attempts: List[ProviderAttempt]
) {
  def fallbackProvider: LlmProvider = primaryProvider match {
    case LlmProvider.Gemini     => LlmProvider.OpenRouter
    case LlmProvider.OpenRouter => LlmProvider.Gemini
  }
}

object Completion {

  def started(prompt: String, primaryProvider: LlmProvider): Completion =
    Completion(
      prompt = prompt,
      primaryProvider = primaryProvider,
      status = CompletionStatus.Pending,
      reply = None,
      attempts = List(ProviderAttempt(primaryProvider, None))
    )

  /** Applies the rules of `llm.allium` to a provider response. Responses for a terminal completion are ignored.
    *
    * A rate limit or server error is retried only when it comes from the primary provider and the fallback has not been
    * attempted yet: this creates the fallback attempt and keeps the completion pending. The same result from the
    * fallback (or any other non-retryable result) fails the completion.
    */
  def acceptResponse(
      completion: Completion,
      provider: LlmProvider,
      response: ProviderResponse
  ): Completion =
    if completion.status != CompletionStatus.Pending then completion
    else if !completion.attempts.exists(_.provider == provider) then completion
    else {
      val withResult = completion.attempts.map { attempt =>
        if attempt.provider == provider then attempt.copy(result = Some(response.result)) else attempt
      }
      response.result match {
        case ProviderResult.Success if response.reply.nonEmpty =>
          completion.copy(
            status = CompletionStatus.Answered,
            reply = response.reply,
            attempts = withResult
          )
        case ProviderResult.Success =>
          // A success without a reply is not a structured reply: it fails the completion.
          completion.copy(status = CompletionStatus.Failed, attempts = withResult)
        case ProviderResult.MalformedJson | ProviderResult.Rejected =>
          completion.copy(status = CompletionStatus.Failed, attempts = withResult)
        case ProviderResult.RateLimited | ProviderResult.ServerError =>
          val providerHasAttempt = withResult.exists(_.provider == provider)
          if provider == completion.primaryProvider && !withResult.exists(_.provider == completion.fallbackProvider)
          then
          // The primary was rate limited or failed: create the one allowed fallback attempt.
          completion.copy(
            attempts = withResult :+ ProviderAttempt(completion.fallbackProvider, None)
          )
          else if provider != completion.primaryProvider && !providerHasAttempt then
          // An unsolicited response from a provider that was never asked: no rule applies, keep the completion.
          completion
          else
            // The fallback was asked and failed too: the completion is exhausted.
            completion.copy(status = CompletionStatus.Failed, attempts = withResult)
      }
    }
}
