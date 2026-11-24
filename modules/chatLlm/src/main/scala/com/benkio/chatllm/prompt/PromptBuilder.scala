package com.benkio.chatllm.prompt

import com.benkio.chatcore.model.Message
import com.benkio.chatllm.Truncate

/** Builds the prompt of one completion: persona + catalogue of the candidate replies + chat context + the message, on
  * top of the versioned template `prompts/<version>/system.md`.
  */
final class PromptBuilder private (template: String, val version: String, val maxReplyChars: Int) {

  def build(persona: String, catalogue: List[CatalogueEntry], context: String, msg: Message): String = {
    val content = msg.text.orElse(msg.caption).getOrElse("")
    val entries =
      if catalogue.isEmpty then "(nessuna voce)"
      else catalogue.map(e => s"${e.id}: ${OutputGuard.untrusted(e.summary)}").mkString("\n")
    // Placeholders are replaced in one pass, so a value containing `{{...}}` is never expanded again.
    val values = Map(
      "max_chars" -> maxReplyChars.toString,
      "persona"   -> persona.trim,
      "catalogue" -> entries,
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
  val currentVersion: String = "v1"
  private val placeholder    = "\\{\\{(\\w+)\\}\\}".r

  def apply(maxReplyChars: Int, version: String = currentVersion): Either[String, PromptBuilder] = {
    val path = s"/prompts/$version/system.md"
    Option(getClass.getResourceAsStream(path))
      .toRight(s"Missing prompt template $path")
      .map(in => scala.util.Using.resource(in)(s => new String(s.readAllBytes(), "UTF-8")))
      .map(new PromptBuilder(_, version, maxReplyChars))
  }
}
