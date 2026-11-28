package com.benkio.chatllm

import cats.effect.Async
import cats.syntax.all.*
import cats.Applicative
import com.benkio.chatcore.llm.Persona
import com.benkio.chatcore.messagefiltering.*
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message
import log.effect.LogWriter
import org.llm4s.llmconnect.model.Conversation
import org.llm4s.llmconnect.model.SystemMessage

/** The chatCore-owned port the [[com.benkio.chatcore.LlmEngine]] uses to ask for an LLM answer. chatLlm supplies the
  * implementation; chatCore never sees the LLM library.
  */
trait LlmResponder[F[_]] {

  /** The answer to send for `msg`, playing the `persona`, or `None` when the LLM has nothing to say. */
  def respond(persona: Persona, msg: Message): F[Option[Text]]
}

object LlmResponder {

  /** The reply of the LLM is the same fixed one for now: the persona and the system prompt are composed, the answer
    * says the feature is in development.
    */
  def apply[F[_]: Async](clients: LlmFallbackClients, systemPrompt: SystemPrompt)(using
      log: LogWriter[F]
  ): LlmResponder[F] =
    LlmResponderImpl[F](clients = clients, systemPrompt = systemPrompt)

  /** Until the LLM replies are rebuilt: never answers, so the engine keeps the outcome it already had. */
  def noop[F[_]: Applicative]: LlmResponder[F] = new LlmResponder[F] {
    override def respond(_persona: Persona, _msg: Message): F[Option[Text]] = Applicative[F].pure(None)
  }

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

    override def respond(persona: Persona, msg: Message): F[Option[Text]] = {
      val content                       = msg.getContent.getOrElse("")
      val conversation: F[Conversation] = Async[F]
        .fromEither(
          Conversation
            .fromPrompts(
              systemPrompt = systemPrompt.render(
                persona = persona.text,
                maxChars = maxChars,
                context = "",
                message = content
              ),
              userPrompt = content
            )
            .leftMap(error => LlmConversationFailed(content, error.message))
            .map(
              _.addMessage(
                SystemMessage(
                  "You have to reply by telling the user that this feature is still in development. Use the persona provided and you may use the user message content"
                )
              )
            )
        )
      for {
        conversation <- conversation
        completion   <- Async[F].blocking(clients.ask(conversation)).flatMap(Async[F].fromEither(_))
        _            <- log.debug(s"[LlmResponder] answered by the LLM: ${completion.content.length} characters")
      } yield Some(Text(completion.content))
    }
  }
}
