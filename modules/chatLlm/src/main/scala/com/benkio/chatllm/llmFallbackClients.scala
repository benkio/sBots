package com.benkio.chatllm

import cats.syntax.either.*
import org.llm4s.llmconnect.model.*
import org.llm4s.llmconnect.model.Conversation
import org.llm4s.llmconnect.LLMClient
import org.llm4s.reliability.ReliabilityConfig
import org.llm4s.reliability.ReliableClient

final case class LlmCallFailed(conversation: Conversation, message: String)
    extends Throwable(s"[FallbackClients] ❌ Call Llm failed: $message")

/** The default provider and the other configured ones, each retried on its own: a fallback is asked only when the
  * previous one still fails.
  */
final class LlmFallbackClients(primary: LLMClient, fallbacks: List[LLMClient], config: ReliabilityConfig) {
  // Build these once and keep them: each circuit breaker counts failures across calls.
  private val first  = "primary" -> ReliableClient(primary, "primary", config)
  private val others = fallbacks.zipWithIndex.map { (client, index) =>
    val name = s"fallback-$index"
    name -> ReliableClient(client, name, config)
  }

  def ask(conversation: Conversation): Either[LlmCallFailed, Completion] = {
    def attempt(provider: (String, ReliableClient)) =
      provider._2.complete(conversation)
    others
      .foldLeft(attempt(first))((served, fallback) => served.orElse(attempt(fallback)))
      .leftMap(error => LlmCallFailed(conversation, error.message))
  }
}
