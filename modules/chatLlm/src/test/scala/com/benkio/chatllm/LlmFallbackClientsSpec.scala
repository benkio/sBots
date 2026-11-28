package com.benkio.chatllm

import munit.FunSuite
import org.llm4s.error.LLMError
import org.llm4s.error.ProcessingError
import org.llm4s.llmconnect.model.AssistantMessage
import org.llm4s.llmconnect.model.Completion
import org.llm4s.llmconnect.model.CompletionOptions
import org.llm4s.llmconnect.model.Conversation
import org.llm4s.llmconnect.model.StreamedChunk
import org.llm4s.llmconnect.model.UserMessage
import org.llm4s.llmconnect.LLMClient
import org.llm4s.reliability.ReliabilityConfig
import org.llm4s.reliability.RetryPolicy
import org.llm4s.types.ProviderModelTypes.ModelName

import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

class LlmFallbackClientsSpec extends FunSuite {

  /** A client that answers `answer`, or fails when it has none, and counts how many times it is asked. */
  private final class StubClient(answer: Option[String]) extends LLMClient {
    val calls = new AtomicInteger(0)

    override def complete(
        conversation: Conversation,
        options: CompletionOptions
    ): Either[LLMError, Completion] = {
      val _ = calls.incrementAndGet()
      answer.toRight(ProcessingError("stub", "the stub fails")).map { text =>
        Completion("id", Instant.now(), ModelName("stub"), AssistantMessage(Some(text)))
      }
    }

    override def streamComplete(
        conversation: Conversation,
        options: CompletionOptions
    )(onChunk: StreamedChunk => Unit): Either[LLMError, Completion] = complete(conversation, options)

    override def contextWindow: Int     = 1000
    override def reserveCompletion: Int = 100
  }

  // one attempt per provider: the retries of llm4s are not what is tested here
  private val config = ReliabilityConfig(retryPolicy = RetryPolicy.noRetry, deadline = None)

  private val question = Conversation(Seq(UserMessage("hi")))

  private def clients(primary: StubClient, fallbacks: StubClient*) =
    LlmFallbackClients(primary, fallbacks.toList, config)

  test("the default provider answers when it works, and the fallbacks are not asked") {
    val primary  = StubClient(Some("from primary"))
    val fallback = StubClient(Some("from fallback"))
    assertEquals(clients(primary, fallback).ask(question).map(_.content), Right("from primary"))
    assertEquals(fallback.calls.get, 0)
  }

  test("the first fallback that works answers when the default provider fails") {
    val primary = StubClient(None)
    val first   = StubClient(None)
    val second  = StubClient(Some("from second"))
    val third   = StubClient(Some("from third"))
    assertEquals(clients(primary, first, second, third).ask(question).map(_.content), Right("from second"))
    assertEquals(third.calls.get, 0)
  }

  test("the question fails when every provider fails") {
    val result = clients(StubClient(None), StubClient(None)).ask(question)
    assertEquals(result.left.map(_.question), Left("hi"))
  }
}
