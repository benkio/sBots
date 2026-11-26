package com.benkio.chatllm.prompt

import com.benkio.chatcore.model.Message
import com.benkio.chatllm.Truncate

/** Builds the prompt of one completion: persona + chat context + the message, on top of the template
  * `prompts/system.md`, modified in place: git holds its history.
  */
final class PromptBuilder private (template: String, val maxReplyChars: Int) {

  def build(persona: String, context: String, msg: Message): String = {
    val content = msg.text.orElse(msg.caption).getOrElse("")
    // Placeholders are replaced in one pass, so a value containing `{{...}}` is never expanded again.
    val values = Map(
      "max_chars" -> maxReplyChars.toString,
      "persona"   -> persona.trim,
      "context"   -> (if context.isBlank then "(vuota)"
                    else context.linesIterator.map(OutputGuard.untrusted).mkString("\n")),
      "message" -> Truncate.utf16(
        OutputGuard.untrusted(Message.redactMentions(content, msg.mentions)),
        PromptBudget.messageReserveBytes
      )
    )
    PromptBuilder.placeholder.replaceAllIn(
      template,
      m => scala.util.matching.Regex.quoteReplacement(values.getOrElse(m.group(1), m.matched))
    )
  }
}

object PromptBuilder {

  private val placeholder = "\\{\\{(\\w+)\\}\\}".r

  def apply(maxReplyChars: Int): Either[String, PromptBuilder] = {
    val path = "/prompts/system.md"
    Option(getClass.getResourceAsStream(path))
      .toRight(s"Missing prompt template $path")
      .map(in => scala.util.Using.resource(in)(s => new String(s.readAllBytes(), "UTF-8")))
      .map(new PromptBuilder(_, maxReplyChars))
  }
}
