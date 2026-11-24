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

  def sanitise(raw: String, maxChars: Int): Option[String] =
    check(raw, maxChars).toOption

  /** The same decision as [[sanitise]], with the rule that rejected the text. The reason is safe to log: the text is
    * not.
    */
  def check(raw: String, maxChars: Int): Either[String, String] = {
    val text = raw.trim
    if maxChars <= 0 then Left("no_capacity")
    else if text.isEmpty then Left("empty_text")
    else if command.findFirstIn(text).nonEmpty then Left("command")
    // A Discord-style raw mention also matches the plain mention pattern, so name the more specific rule first.
    else if rawMention.findFirstIn(text).nonEmpty then Left("raw_mention")
    else if mention.findFirstIn(text).nonEmpty then Left("mention")
    else Right(truncate(text, maxChars))
  }

  /** Caps by code points, so an emoji (a surrogate pair) is never cut in half. */
  private def truncate(text: String, maxChars: Int): String =
    if text.codePointCount(0, text.length) <= maxChars then text
    else text.substring(0, text.offsetByCodePoints(0, maxChars - 1)).trim + "…"

  /** Text that is not the model's but goes into the prompt: no newline that forges a section, no mention. */
  def untrusted(content: String): String =
    Message.singleLine(Message.redactMentions(content, List.empty))
}
