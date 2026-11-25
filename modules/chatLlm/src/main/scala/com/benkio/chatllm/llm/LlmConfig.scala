package com.benkio.chatllm.llm

import cats.effect.Async
import com.benkio.chatllm.prompt.PromptBudget
import pureconfig.*

import scala.concurrent.duration.*

/** A secret value that redacts itself in logs and toString. */
final case class Sensitive(value: String) {
  override def toString: String = "***"
}

object Sensitive {
  given ConfigReader[Sensitive] = ConfigReader.stringConfigReader.map(Sensitive(_))
}

/** Connection details for one provider. The endpoint is a full URI rather than a host, so both the public APIs and a
  * local stub can be configured with the same shape. Each provider carries its own quota: a fallback request counts
  * against the fallback provider, never the primary.
  */
final case class ProviderConnection(
    endpoint: String,
    model: Option[String] = None,
    apiKey: Option[Sensitive] = None,
    quota: LlmBudgetConfig = LlmBudgetConfig()
) derives ConfigReader

/** The client-side quota of one provider. Both windows slide: a request that is more than a minute (or a day) old no
  * longer counts. The values mirror the provider's documented free-tier limits and are revisable without code changes.
  */
final case class LlmBudgetConfig(
    maxPerMinute: Int = 20,
    maxPerDay: Int = 50
) derives ConfigReader

/** #1489: the per-chat context window, its retention and the anonymous sender labels. The token budget uses the same
  * four-characters-per-token estimate as the input limit, so a window can never make a prompt exceed that limit.
  */
final case class LlmContextConfig(
    maxMessages: Int = LlmContextConfig.defaultMaxMessages,
    maxTokens: Int = LlmContextConfig.defaultMaxTokens,
    ttl: FiniteDuration = LlmContextConfig.defaultTtl,
    cleanupCron: String = LlmContextConfig.defaultCleanupCron,
    // Only a placeholder: validation rejects it, so a deployment cannot inherit a publicly known secret.
    authorSalt: Sensitive = Sensitive(LlmContextConfig.publicExampleSalt)
) derives ConfigReader {

  /** The epoch second before which a message is expired: the single definition used by the cleanup, the window and
    * `deleteExpired`.
    */
  def cutoff(nowEpochSecond: Long): Long = nowEpochSecond - ttl.toSeconds

  /** The rows kept per chat and bot: older ones are trimmed on write, so the table cannot grow without bound inside the
    * TTL. Far above the window, so the trim never shortens what a prompt can use.
    */
  def retainedMessages: Int = maxMessages * LlmContextConfig.retentionFactor

  /** The longest stored message, in characters: the whole token budget, so the newest message alone can never exceed
    * the window and erase it (a message is cut, not dropped).
    */
  def maxMessageChars: Int = math.max(1, maxTokens * 4)

  // The salt is a secret: knowing it would let anyone with source or log access brute-force the sender hashes back to
  // ids. It must come from the deployment, never from a shipped default.
  override def toString: String =
    s"LlmContextConfig($maxMessages,$maxTokens,$ttl,$cleanupCron,${LlmContextConfig.redacted})"
}

/** #1493: the opt-in debug trace. When `enabled`, the full prompt and the raw response of every completion are stored
  * in `llm_trace` for `ttl`; they contain chat content, so the flag is off by default and the retention is short.
  */
final case class LlmTraceConfig(
    enabled: Boolean = false,
    ttl: FiniteDuration = LlmTraceConfig.defaultTtl
) derives ConfigReader {
  def cutoff(nowEpochSecond: Long): Long = nowEpochSecond - ttl.toSeconds
}

object LlmTraceConfig {
  val defaultTtl: FiniteDuration = 1.day
}

/** The section `main.llm` of the application configuration. */
final case class LlmConfig(
    gemini: ProviderConnection,
    openrouter: ProviderConnection,
    primaryProvider: Option[String] = None,
    timeout: FiniteDuration = LlmConfig.defaultTimeout,
    providerTimeout: FiniteDuration = LlmConfig.defaultProviderTimeout,
    maxInputTokens: Int = LlmConfig.defaultMaxInputTokens,
    maxOutputTokens: Int = LlmConfig.defaultMaxOutputTokens,
    context: LlmContextConfig = LlmContextConfig(),
    trace: LlmTraceConfig = LlmTraceConfig()
) derives ConfigReader

object LlmContextConfig {

  val retentionFactor: Int       = 10
  val defaultMaxMessages: Int    = 20
  val defaultMaxTokens: Int      = 2048
  val defaultTtl: FiniteDuration = 7.days
  val defaultCleanupCron: String = "0 0 * ? * *"

  /** Not a usable default: it is public, so validation rejects it. Every deployment must set its own salt. */
  val publicExampleSalt: String = "sbots-llm"
  private val redacted: String  = "<redacted>"

  /** The salt must be stable across restarts: a changing salt would change every sender label and break the history. */
  def validate(config: LlmContextConfig, maxInputTokens: Int): Either[String, Unit] = {
    val problems = List(
      if config.maxMessages <= 0 then Some("llm.context.max-messages must be positive") else None,
      if config.maxTokens <= 0 then Some("llm.context.max-tokens must be positive") else None,
      if config.maxTokens > maxInputTokens then Some("llm.context.max-tokens must not exceed llm.max-input-tokens")
      else None,
      if PromptBudget.personaMaxBytes(maxInputTokens, config.maxTokens) <= 0 then Some(
        "llm.max-input-tokens leaves no room for a persona next to the template, the catalogue, the message and " +
          "llm.context.max-tokens: raise llm.max-input-tokens or lower llm.context.max-tokens"
      )
      else None,
      if config.ttl < 1.second then Some("llm.context.ttl must be at least one second")
      else None,
      if cron4s.Cron(config.cleanupCron).isLeft then Some("llm.context.cleanup-cron must be a valid cron expression")
      else None,
      if config.authorSalt.value.isBlank then Some("llm.context.author-salt must not be blank")
      else if config.authorSalt.value.trim == publicExampleSalt then Some(
        "llm.context.author-salt must not be the public example value: set LLM_CONTEXT_AUTHOR_SALT"
      )
      else None
    ).flatten
    Either.cond(problems.isEmpty, (), problems.mkString(", "))
  }
}

object LlmConfig {

  val defaultTimeout: FiniteDuration         = com.benkio.chatcore.llm.LlmEngine.defaultTimeout
  val defaultProviderTimeout: FiniteDuration = 4.seconds
  val defaultMaxInputTokens: Int             = 18432
  val defaultMaxOutputTokens: Int            = 512
  val defaultMaxPerMinute: Int               = 20
  val defaultMaxPerDay: Int                  = 50

  /** The primary provider is the one asked first; each provider's own quota governs whether it may be called. */
  def primary(config: LlmConfig): Either[String, LlmProvider] =
    LlmProvider.fromString(config.primaryProvider)

  /** The configured quota for a provider, used by the shared limiter to admit or refuse a provider call. */
  def quotaFor(config: LlmConfig)(provider: LlmProvider): LlmBudgetConfig =
    provider match {
      case LlmProvider.Gemini     => config.gemini.quota
      case LlmProvider.OpenRouter => config.openrouter.quota
    }

  /** The fallback can call either provider, so both keys are needed before any request is admitted. Without them the
    * client fails completions locally instead of sending anonymous requests that the providers reject.
    */
  def credentialsComplete(config: LlmConfig): Boolean =
    hasKey(config.gemini.apiKey) && hasKey(config.openrouter.apiKey)

  /** A key counts only when it carries at least one non-whitespace character: a blank secret must not admit a request.
    */
  private def hasKey(key: Option[Sensitive]): Boolean =
    key.exists(_.value.exists(c => !c.isWhitespace))

  /** Invalid tuning makes the startup fail instead of silently disabling the limits. Each provider's quota is checked
    * separately so a bad override cannot disable one provider silently.
    */
  def validate(config: LlmConfig): Either[String, Unit] = {
    val problems = List(
      if config.timeout.length <= 0 then Some("llm.timeout must be positive") else None,
      if config.providerTimeout.length <= 0 then Some("llm.provider-timeout must be positive")
      else if config.providerTimeout >= config.timeout then Some(
        s"llm.provider-timeout (${config.providerTimeout}) must be shorter than llm.timeout (${config.timeout}): " +
          "otherwise a timed-out primary leaves no time for the fallback"
      )
      else None,
      if config.maxInputTokens <= 0 then Some("llm.max-input-tokens must be positive") else None,
      if config.maxOutputTokens <= 0 then Some("llm.max-output-tokens must be positive") else None,
      if config.gemini.quota.maxPerMinute <= 0 then Some("llm.gemini.quota.max-per-minute must be positive") else None,
      if config.gemini.quota.maxPerDay <= 0 then Some("llm.gemini.quota.max-per-day must be positive") else None,
      if config.openrouter.quota.maxPerMinute <= 0 then Some("llm.openrouter.quota.max-per-minute must be positive")
      else None,
      if config.openrouter.quota.maxPerDay <= 0 then Some("llm.openrouter.quota.max-per-day must be positive")
      else None,
      if config.trace.ttl < 1.second then Some("llm.trace.ttl must be at least one second") else None
    ).flatten
    if problems.nonEmpty then Left(problems.mkString(", "))
    else LlmContextConfig.validate(config.context, config.maxInputTokens)
  }

  def load[F[_]: Async]: F[LlmConfig] =
    ConfigSource.default
      .at("main.llm")
      .load[LlmConfig]
      .fold(
        err => Async[F].raiseError[LlmConfig](new RuntimeException(err.prettyPrint())),
        config =>
          validate(config) match {
            case Left(err) => Async[F].raiseError[LlmConfig](new IllegalArgumentException(err))
            case Right(()) => Async[F].pure(config)
          }
      )
}
