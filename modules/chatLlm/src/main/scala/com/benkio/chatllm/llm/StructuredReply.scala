package com.benkio.chatllm.llm

import cats.syntax.all.*
import io.circe.parser

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

  def parse(raw: String): Either[String, LlmReply] =
    parser.parse(raw).left.map(_.getMessage).flatMap { json =>
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
        } yield LlmReply(text.getOrElse(""), action, replyId)
      }
    }
}
