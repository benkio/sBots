package com.benkio.chatllm.prompt

import com.benkio.chatcore.model.Message

/** The rules the model's text must respect before the bot sends it: length cap, no command, no mention of anybody. A
  * text breaking a rule is dropped rather than repaired (`None`), except the length cap which truncates.
  */
object OutputGuard {

  private val mention    = "(?<![\\w])@\\w".r
  private val rawMention = "<@[!&]?\\d+>".r
  // A command is a line starting with one or more / or ! markers followed by a letter, so doubled prefixes (!!ban, //x)
  // are rejected too, while "!!! che bello" or "1/2" are plain text.
  private val command = "(?m)^\\s*[/!]+\\p{L}".r

  def sanitise(raw: String, maxChars: Int): Option[String] = {
    val text = raw.trim
    Option.when(
      maxChars > 0 &&
        text.nonEmpty &&
        command.findFirstIn(text).isEmpty &&
        mention.findFirstIn(text).isEmpty &&
        rawMention.findFirstIn(text).isEmpty
    )(truncate(text, maxChars))
  }

  /** Caps by code points, so an emoji (a surrogate pair) is never cut in half. */
  private def truncate(text: String, maxChars: Int): String =
    if text.codePointCount(0, text.length) <= maxChars then text
    else text.substring(0, text.offsetByCodePoints(0, maxChars - 1)).trim + "…"

  /** Text that is not the model's but goes into the prompt: no newline that forges a section, no mention. */
  def untrusted(content: String): String =
    Message.singleLine(Message.redactMentions(content, List.empty))
}
