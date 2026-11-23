package com.benkio.chatllm.llm

import io.circe.parser

/** Both providers are asked for a JSON object with a single `text` field: the response is accepted only when that field
  * is present and is a string.
  */
private[llm] object StructuredReply {
  private val TextKey = "text"

  def parse(raw: String): Either[String, LlmReply] =
    parser.parse(raw).left.map(_.getMessage).flatMap { json =>
      json.asObject
        .flatMap(_.apply(TextKey))
        .flatMap(_.asString)
        .toRight(s"Missing string field '$TextKey' in LLM reply: $raw")
        .map(LlmReply(_))
    }
}
