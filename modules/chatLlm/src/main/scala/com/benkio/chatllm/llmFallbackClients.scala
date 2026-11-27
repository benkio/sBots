package com.benkio.chatllm

import cats.syntax.either.*
import org.llm4s.llmconnect.model.Conversation
import org.llm4s.llmconnect.model.UserMessage
import org.llm4s.llmconnect.LLMClient
import org.llm4s.reliability.ReliabilityConfig
import org.llm4s.reliability.ReliableClient
import org.llm4s.reliability.RetryPolicy

import scala.concurrent.duration.*

final case class LlmQuestionFailed(question: String, message: String)
    extends Throwable(s"[FallbackClients] ❌ Question failed: question: '$question' - error: $message")

/** The answer to a question and the provider that gave it. */
final case class Served(provider: String, content: String)

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

  def ask(question: String): Either[LlmQuestionFailed, Served] = {
    val conversation                                = Conversation(Seq(UserMessage(question)))
    def attempt(provider: (String, ReliableClient)) =
      provider._2.complete(conversation).map(completion => Served(provider._1, completion.content))
    others
      .foldLeft(attempt(first))((served, fallback) => served.orElse(attempt(fallback)))
      .leftMap(error => LlmQuestionFailed(question, error.message))
  }
}

object LlmFallbackClients {
  // TODO: eventually load this from the configs
  val reliabilityConfig: ReliabilityConfig = ReliabilityConfig(
    retryPolicy = RetryPolicy.fixedDelay(maxAttempts = 2, delay = 200.millis),
    deadline = Some(10.seconds)
  )
}
