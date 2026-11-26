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
  * One narrow recovery is applied before giving up, reported to observability: small models wrap the object in a fence
  * or prose, and that is stripped. A truncated or otherwise unreadable object is never recovered: it is rejected and
  * logged as malformed.
  */
private[llm] object StructuredReply {
  private val TextKey   = "text"
  private val ActionKey = "action"

  /** How the raw model text had to be read. Observability reports it; the reply is validated strictly in every case. */
  enum Repair {
    case AsIs, Stripped
  }

  final case class Parsed(reply: LlmReply, repair: Repair)

  def parse(raw: String): Either[String, LlmReply] = parseDetailed(raw).map(_.reply)

  def parseDetailed(raw: String): Either[String, Parsed] =
    parseJson(raw).fold(
      Left(_),
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
}
