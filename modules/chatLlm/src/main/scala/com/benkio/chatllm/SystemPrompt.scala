package com.benkio.chatllm

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.repository.Repository
import log.effect.LogWriter

import java.nio.charset.StandardCharsets
import scala.util.matching.Regex

/** The system prompt template of the LLM replies (`prompts/system.md`): the rules of the bot, with the placeholders
  * `{{persona}}`, `{{max_chars}}`, `{{context}}` and `{{message}}` that [[render]] fills. chatLlm owns it; the persona
  * is the only part that comes from the bot.
  */
final case class SystemPrompt(template: String) {

  /** Fills the placeholders in a single pass: the values are inserted as they are, never searched for placeholders, so
    * a persona or a message cannot inject one. An unknown placeholder is left as it is.
    */
  def render(persona: String, maxChars: Int, context: String, message: String): String = {
    val values = Map(
      "persona"   -> persona,
      "max_chars" -> maxChars.toString,
      "context"   -> context,
      "message"   -> message
    )
    SystemPrompt.placeholder.replaceAllIn(
      template,
      found => Regex.quoteReplacement(values.getOrElse(found.group(1), found.matched))
    )
  }
}

object SystemPrompt {

  final case class EmptySystemPrompt(resource: String)
      extends Throwable(s"[SystemPrompt] The system prompt $resource is empty")

  val resource: String = "prompts/system.md"

  private val placeholder: Regex = "\\{\\{(\\w+)\\}\\}".r

  /** Reads the template from the resources of chatLlm. A missing or empty file is an error. */
  def load[F[_]: Async: LogWriter]: F[SystemPrompt] =
    Repository
      .getResourceByteArray[F](resource)
      .use(Async[F].fromEither(_))
      .map(bytes => String(bytes, StandardCharsets.UTF_8))
      .flatMap(text => Async[F].raiseWhen(text.trim.isEmpty)(EmptySystemPrompt(resource)).as(SystemPrompt(text)))
}
