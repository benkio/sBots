package com.benkio.chatllm.llm

import cats.syntax.all.*
import io.circe.parser
import io.circe.Json

/** Both providers are asked for a JSON object with a required `action` (`text`, `reuse` or `silent`) and the optional
  * `text` and `replyId` strings. The response is accepted only when `action` is valid and any present `text` or
  * `replyId` is a string.
  *
  * A reply that parses but breaks this contract (unknown action, non-string field) is reported as malformed JSON like
  * any other unusable provider output: the completion then follows the normal failure path (fallback provider, and
  * silent when that fails too). It is not special-cased as a silent decision.
  */
private[llm] object StructuredReply {
  private val TextKey    = "text"
  private val ActionKey  = "action"
  private val ReplyIdKey = "replyId"

  def parse(raw: String): Either[String, LlmReply] = parseDetailed(raw).map(_._1)

  /** The reply, and whether it parsed only after the fence/prose around the JSON object was cut away. */
  def parseDetailed(raw: String): Either[String, (LlmReply, Boolean)] =
    parseJson(raw).flatMap { case (json, stripped) =>
      json.asObject.toRight(s"LLM reply is not a JSON object: $raw").flatMap { obj =>
        def optional(key: String): Either[String, Option[String]] =
          obj(key).filter(!_.isNull).traverse(_.asString.toRight(s"Field '$key' is not a string in LLM reply: $raw"))
        for {
          action <- obj(ActionKey)
            .flatMap(_.asString)
            .flatMap(LlmAction.parse)
            .toRight(s"Missing or invalid field '$ActionKey' in LLM reply: $raw")
          text    <- optional(TextKey)
          replyId <- optional(ReplyIdKey)
        } yield (LlmReply(text.getOrElse(""), action, replyId), stripped)
      }
    }

  /** Small models often wrap the object in a markdown fence or add a sentence around it: when the raw text is not JSON
    * as is, the span from the first `{` to the last `}` is tried. The result is still validated strictly.
    */
  private def parseJson(raw: String): Either[String, (Json, Boolean)] =
    parser.parse(raw).map(_ -> false).left.flatMap { error =>
      val start = raw.indexOf('{')
      val end   = raw.lastIndexOf('}')
      if start >= 0 && end > start then parser
        .parse(raw.substring(start, end + 1))
        .map(_ -> true)
        .left
        .map(_ => error.getMessage)
      else Left(error.getMessage)
    }
}
