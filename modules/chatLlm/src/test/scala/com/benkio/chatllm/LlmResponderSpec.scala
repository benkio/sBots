package com.benkio.chatllm

import cats.effect.IO
import com.benkio.chatcore.llm.Persona
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite
import org.llm4s.error.LLMError
import org.llm4s.llmconnect.model.AssistantMessage
import org.llm4s.llmconnect.model.Completion
import org.llm4s.llmconnect.model.CompletionOptions
import org.llm4s.llmconnect.model.Conversation
import org.llm4s.llmconnect.model.StreamedChunk
import org.llm4s.llmconnect.model.SystemMessage
import org.llm4s.llmconnect.LLMClient
import org.llm4s.reliability.ReliabilityConfig
import org.llm4s.reliability.RetryPolicy
import org.llm4s.types.ProviderModelTypes.ModelName

import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

class LlmResponderSpec extends CatsEffectSuite {

  private val persona = Persona("Sei il bot di prova.")
  private val message = Message(
    messageId = None,
    date = 0L,
    chatId = ChatId(Platform.Telegram, "1"),
    chatType = "private",
    text = Some("hello")
  )

  private val systemPrompt = SystemPrompt("PERSONA: {{persona}}\nMESSAGGIO: {{message}}")

  private val config = ReliabilityConfig(retryPolicy = RetryPolicy.noRetry, deadline = None)

  /** A client that records the conversation and answers with the given envelope. */
  private final class RecordingClient(envelope: String) extends LLMClient {
    private val askedRef = new AtomicReference[Option[Conversation]](None)

    def asked: Option[Conversation] = askedRef.get()

    override def complete(
        conversation: Conversation,
        options: CompletionOptions
    ): Either[LLMError, Completion] = {
      val _ = askedRef.set(Some(conversation))
      Right(Completion("id", Instant.now(), ModelName("stub"), AssistantMessage(Some(envelope))))
    }

    override def streamComplete(
        conversation: Conversation,
        options: CompletionOptions
    )(onChunk: StreamedChunk => Unit): Either[LLMError, Completion] = complete(conversation, options)

    override def contextWindow: Int     = 1000
    override def reserveCompletion: Int = 100
  }

  private def responder(client: RecordingClient): LlmResponder[IO] =
    LlmResponder[IO](LlmFallbackClients(client, Nil, config), systemPrompt)

  test("the provider gets one system message with the prompt and the development instruction") {
    val client = new RecordingClient("""{"action":"silent","text":""}""")
    for {
      reply <- responder(client).respond(persona, message)
    } yield {
      assertEquals(reply, None)
      assertEquals(client.asked.map(_.messages.size), Some(2))
      assertEquals(
        client.asked
          .flatMap(_.messages.collectFirst { case SystemMessage(text) => text })
          .exists(text => text.contains("Sei il bot di prova.") && text.contains("still in development")),
        true
      )
      assertEquals(client.asked.flatMap(_.messages.lastOption.map(_.content)), Some("hello"))
    }
  }

  test("the text envelope becomes a text reply") {
    val client = new RecordingClient("""{"action":"text","text":"ciao"}""")
    responder(client).respond(persona, message).map(reply => assertEquals(reply, Some(Text("ciao"))))
  }

  test("an invalid envelope fails the responder") {
    val client = new RecordingClient("not json")
    interceptIO[LlmResponder.LlmReplyInvalid](responder(client).respond(persona, message)).void
  }

  test("an unsupported action fails the responder") {
    val client = new RecordingClient("""{"action":"unknown","text":""}""")
    interceptIO[LlmResponder.LlmReplyInvalid](responder(client).respond(persona, message)).void
  }
}
