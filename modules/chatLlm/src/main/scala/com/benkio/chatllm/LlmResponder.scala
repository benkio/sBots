package com.benkio.chatllm

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.llm.Persona
import com.benkio.chatcore.messagefiltering.*
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message
import log.effect.LogWriter
import org.llm4s.llmconnect.model.Conversation

/** The chatCore-owned port the [[com.benkio.chatcore.LlmEngine]] uses to ask for an LLM answer. chatLlm supplies the
  * implementation; chatCore never sees the LLM library.
  */
trait LlmResponder[F[_]] {

  /** The answer to send for `msg`, playing the `persona` */
  def respond(persona: Persona, msg: Message): F[Text]
}

object LlmResponder {

  /** The active responder: asks the configured providers with the persona, system prompt and the single message, then
    * returns the provider's plain text.
    */
  def apply[F[_]: Async](clients: LlmFallbackClients, systemPrompt: SystemPrompt)(using
      log: LogWriter[F]
  ): LlmResponder[F] =
    LlmResponderImpl[F](clients = clients, systemPrompt = systemPrompt)

  /** The most characters the system prompt lets a reply have. */
  private val maxChars = 500

  /** Building the conversation failed before any provider was called, so there is no Conversation to attach. */
  private[chatllm] final case class LlmConversationFailed(question: String, message: String)
      extends Throwable(s"[LlmResponder] ❌ Cannot build the conversation: $message")

  private[chatllm] final case class LlmResponderImpl[F[_]: Async](
      clients: LlmFallbackClients,
      systemPrompt: SystemPrompt
  )(using log: LogWriter[F])
      extends LlmResponder[F] {

    override def respond(persona: Persona, msg: Message): F[Text] = {
      val content                       = msg.getContent.getOrElse("")
      val conversation: F[Conversation] = Async[F]
        .fromEither(
          Conversation
            .fromPrompts(
              systemPrompt = systemPrompt.render(
                persona = persona.text,
                maxChars = maxChars,
                message = content
              ),
              userPrompt = content
            )
            .leftMap(error => LlmConversationFailed(content, error.message))
        )
      for {
        conversation <- conversation
        completion   <- Async[F].blocking(clients.ask(conversation)).flatMap(Async[F].fromEither(_))
        _            <- log.debug(s"[LlmResponder] answered by the LLM: ${completion.content.length} characters")
      } yield Text(completion.content)
    }
  }
}
