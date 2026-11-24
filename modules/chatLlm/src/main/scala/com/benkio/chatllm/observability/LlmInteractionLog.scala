package com.benkio.chatllm.observability

/** The `[llm]` line of an interaction, in the `logfmt` style of the `[stats]` lines (`docs/monitoring.md`): it travels
  * through the Mon pipeline to Loki and is read with the `regexp` parser. Keep the keys stable: the dashboard depends
  * on them. New keys are appended at the end. A key is always present, `-` meaning "no value", so a regexp can rely on
  * the order.
  */
object LlmInteractionLog {

  val tag: String = "[llm]"

  def line(i: LlmInteraction): String =
    List(
      "type"            -> "interaction",
      "chat"            -> i.chatHash,
      "bot"             -> i.bot,
      "model"           -> i.model,
      "prompt_version"  -> i.promptVersion,
      "prompt_tokens"   -> i.promptTokens.toString,
      "response_tokens" -> i.responseTokens.toString,
      "latency_ms"      -> i.latencyMs.toString,
      "status"          -> i.status,
      "action"          -> i.action.label,
      "reply_id"        -> i.reusedReplyId.getOrElse("-"),
      "failure"         -> i.failure.getOrElse("-"),
      "attempts"        -> (if i.attempts.isEmpty then "-" else i.attempts.mkString(",")),
      "fallback"        -> i.fallback.toString,
      "parsed"          -> i.parsed.toString,
      "stripped"        -> i.stripped.toString,
      "local_rejection" -> i.localRejection.getOrElse("-")
    ).map { case (k, v) => s"$k=${value(v)}" }.mkString(s"$tag ", " ", "")

  /** A value never contains a space or a quote, so the line stays parsable whatever the model or a reply id holds. */
  private def value(v: String): String = {
    val cleaned = v.map(c => if c.isWhitespace || c == '"' || c == '=' then '_' else c)
    if cleaned.isEmpty then "-" else cleaned
  }
}
