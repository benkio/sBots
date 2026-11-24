package com.benkio.chatllm.prompt

import com.benkio.chatllm.llm.LlmAction
import com.benkio.chatllm.llm.LlmReply

/** The output contract `{action: text|reuse|silent, text?, replyId?}` after validation: whatever the model returned,
  * only these three outcomes exist and anything unacceptable is `Silent`.
  */
enum LlmDecision {
  case Text(text: String)
  case Reuse(replyId: String)
  case Silent
}

object LlmDecision {

  /** Interpretation of the structured reply (its action is already a valid `LlmAction`): an empty text, a missing reply
    * id or a text breaking the output rules (see `OutputGuard`) become `Silent`.
    */
  def from(reply: LlmReply, maxChars: Int): LlmDecision =
    reply.action match {
      case LlmAction.Text   => OutputGuard.sanitise(reply.text, maxChars).fold[LlmDecision](Silent)(Text(_))
      case LlmAction.Reuse  => reply.replyId.map(_.trim).filter(_.nonEmpty).fold[LlmDecision](Silent)(Reuse(_))
      case LlmAction.Silent => Silent
    }
}
