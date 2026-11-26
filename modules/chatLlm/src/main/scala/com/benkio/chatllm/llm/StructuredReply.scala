package com.benkio.chatllm.llm

import cats.syntax.all.*
import io.circe.parser
import io.circe.Json

/** Both providers are asked for a JSON object with a required `action` (`text` or `silent`) and the optional `text`
  * string. The response is accepted only when `action` is valid and a present `text` is a string.
  *
  * A reply that parses but breaks this contract (unknown action, non-string field) is reported as malformed JSON like
  * any other unusable provider output: the completion then follows the normal failure path (fallback provider, and
  * silent when that fails too). It is not special-cased as a silent decision.
  *
  * Two recoveries are applied before giving up, both narrow and both reported to observability: small models wrap the
  * object in a fence or prose, and a model that loops until the provider's output-token limit returns an object cut in
  * the middle of `text`. A truncated object is recovered only when the `text` action is unambiguous; the recovered text
  * still goes through the output guard, which caps it at the reply limit.
  *
  * WORKAROUND/TODO: in principle the model must return a well-structured object; anything else should fail as it did
  * before this change. Recovering a cut reply is only a temporary usability/stability measure until generation is fixed
  * at the source. Remove `truncatedText` and the `truncated` observability field once no completion needs it.
  */
private[llm] object StructuredReply {
  private val TextKey   = "text"
  private val ActionKey = "action"

  /** How the raw model text had to be read. Observability reports it; the reply is validated strictly in every case. */
  enum Repair {
    case AsIs, Stripped, Truncated
  }

  final case class Parsed(reply: LlmReply, repair: Repair)

  // WORKAROUND/TODO: temporary stability measure; a malformed reply should still fail, see the object docs above.
  /** The only prefix that can be repaired: `action` is already known and the cut is inside the `text` value. */
  private val truncatedTextPrefix = "\"action\"\\s*:\\s*\"text\"\\s*,\\s*\"text\"\\s*:\\s*\"".r

  def parse(raw: String): Either[String, LlmReply] = parseDetailed(raw).map(_.reply)

  def parseDetailed(raw: String): Either[String, Parsed] =
    parseJson(raw).fold(
      error => truncatedText(raw).toRight(error).map(reply => Parsed(reply, Repair.Truncated)),
      { case (json, repair) =>
        json.asObject.toRight(s"LLM reply is not a JSON object: $raw").flatMap { obj =>
          def optional(key: String): Either[String, Option[String]] =
            obj(key).filter(!_.isNull).traverse(_.asString.toRight(s"Field '$key' is not a string in LLM reply: $raw"))
          for {
            action <- obj(ActionKey)
              .flatMap(_.asString)
              .flatMap(LlmAction.parse)
              .toRight(s"Missing or invalid field '$ActionKey' in LLM reply: $raw")
            text <- optional(TextKey)
          } yield Parsed(LlmReply(text.getOrElse(""), action), repair)
        }
      }
    )

  /** Small models often wrap the object in a markdown fence or add a sentence around it: when the raw text is not JSON
    * as is, the span from the first `{` to the last `}` is tried. The result is still validated strictly.
    */
  private def parseJson(raw: String): Either[String, (Json, Repair)] =
    parser.parse(raw).map(_ -> Repair.AsIs).left.flatMap { error =>
      val start = raw.indexOf('{')
      val end   = raw.lastIndexOf('}')
      if start >= 0 && end > start then parser
        .parse(raw.substring(start, end + 1))
        .map(_ -> Repair.Stripped)
        .left
        .map(_ => error.getMessage)
      else Left(error.getMessage)
    }

  /** A completion stopped by `maxOutputTokens` can end mid-string, with no closing brace. Close the first `text` value
    * and decode it as a JSON string: an incomplete escape is dropped rather than guessed, and anything that still is
    * not a string leaves the reply malformed. Only a `text` action is recovered.
    */
  private def truncatedText(raw: String): Option[LlmReply] =
    truncatedTextPrefix.findFirstMatchIn(raw).flatMap { m =>
      val rest = raw.substring(m.end)
      val text = closeString(rest).filter(_.nonEmpty)
      text.map(t => LlmReply(t, LlmAction.Text))
    }

  /** The characters up to the first unescaped quote, or to the end when the string never closed. A single trailing
    * backslash would escape the quote we add, so it is removed; a paired backslash is real content and stays.
    */
  private def closeString(rest: String): Option[String] = {
    var escaped = false
    var end     = -1
    var index   = 0
    while index < rest.length && end < 0 do {
      val c = rest.charAt(index)
      if escaped then escaped = false
      else
        c match {
          case '\\' => escaped = true
          case '"'  => end = index
          case _    => ()
        }
      index += 1
    }
    val candidate = if end >= 0 then rest.substring(0, end) else rest
    val trailing  = candidate.reverse.takeWhile(_ == '\\').length
    val clean     = if trailing % 2 == 1 then candidate.dropRight(1) else candidate
    parser.parse(s""""$clean"""").toOption.flatMap(_.asString)
  }
}
