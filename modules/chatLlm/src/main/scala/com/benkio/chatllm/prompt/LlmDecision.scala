package com.benkio.chatllm.prompt

import com.benkio.chatllm.llm.LlmAction
import com.benkio.chatllm.llm.LlmReply

/** The output contract `{action: text|silent, text}` after validation: whatever the model returned, only these two
  * outcomes exist and anything unacceptable is `Silent`.
  */
enum LlmDecision {
  case Text(text: String)
  case Silent
}

object LlmDecision {

  /** Interpretation of the structured reply (its action is already a valid `LlmAction`): an empty text or a text
    * breaking the output rules (see `OutputGuard`) become `Silent`.
    */
  def from(reply: LlmReply, maxChars: Int): LlmDecision =
    reply.action match {
      case LlmAction.Text   => OutputGuard.sanitise(reply.text, maxChars).fold[LlmDecision](Silent)(Text(_))
      case LlmAction.Silent => Silent
    }
}
