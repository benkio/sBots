package com.benkio.chatllm.observability

import io.circe.syntax.*
import io.circe.Json

/** The log line of an interaction: one JSON object, which is the `message` of the log record, so it travels through the
  * Mon pipeline to Loki and is read with `| json | line_format "{{.message}}" | json` (`docs/monitoring.md`). Keep the
  * keys stable: the dashboard depends on them. New keys are appended at the end. The keys without a value (`reply_id`,
  * `failure`, `local_rejection`, `provider_errors`, `provider_error`, `gemini_ms`, `openrouter_ms`) are omitted, so a
  * missing label means "none"; `truncated` is likewise absent unless a cut reply was recovered, and the
  * provider-reported `prompt_tokens_reported`, `response_tokens_reported` and `cached_tokens` (#1590) are absent unless
  * the provider reported them.
  */
object LlmInteractionLog {

  val kind: String = "llm_interaction"

  def json(i: LlmInteraction): Json =
    Json
      .obj(
        "type"            -> kind.asJson,
        "chat"            -> i.chatHash.asJson,
        "bot"             -> i.bot.asJson,
        "model"           -> i.model.asJson,
        "prompt_version"  -> i.promptVersion.asJson,
        "prompt_tokens"   -> i.promptTokens.asJson,
        "response_tokens" -> i.responseTokens.asJson,
        "latency_ms"      -> i.latencyMs.asJson,
        "status"          -> i.status.asJson,
        "action"          -> i.action.label.asJson,
        "reply_id"        -> i.reusedReplyId.asJson,
        "failure"         -> i.failure.asJson,
        "attempts"        -> i.attempts.mkString(",").asJson,
        "fallback"        -> i.fallback.asJson,
        "parsed"          -> i.parsed.asJson,
        "stripped"        -> i.stripped.asJson,
        "local_rejection" -> i.localRejection.asJson,
        "provider_errors" -> (if i.providerErrors.isEmpty then None else Some(i.providerErrors.mkString(","))).asJson,
        "provider_error"  -> i.providerErrorMessage.asJson,
        "gemini_ms"       -> i.geminiMs.asJson,
        "openrouter_ms"   -> i.openRouterMs.asJson,
        // appended so dashboards of existing lines keep working; absent on every reply that needed no repair
        "truncated"                -> (if i.truncated then Some(true) else None).asJson,
        "prompt_tokens_reported"   -> i.promptTokensReported.asJson,
        "response_tokens_reported" -> i.responseTokensReported.asJson,
        "cached_tokens"            -> i.cachedTokens.asJson
      )
      .dropNullValues

  def line(i: LlmInteraction): String = json(i).noSpaces
}
