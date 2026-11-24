package com.benkio.chatllm.prompt

/** The shared arithmetic of the prompt size, in bytes at the same four-characters-per-token estimate the input limit
  * uses. The prompt is the template (with its examples), the persona, the catalogue of the candidate replies, the chat
  * context window and the message; whatever the others reserve, the persona gets the rest. Startup validation and the
  * persona check (`PersonaTool`) both use it, so a persona that cannot fit is caught before it silently fails every
  * completion.
  */
object PromptBudget {
  val templateReserveBytes: Int  = 4096
  val catalogueReserveBytes: Int = 8192
  val messageReserveBytes: Int   = 2048

  /** Accented Italian tokenises at about 3 to 3.5 characters per token, so the persona budget assumes 3: the local
    * four-characters check only catches obviously oversized prompts, this one keeps a real prompt under the limit.
    */
  val charsPerToken: Int = 3

  /** The largest persona that fits next to a full context window (stored at up to 4 characters per token). Zero or
    * negative: the limits leave no room.
    */
  def personaMaxBytes(maxInputTokens: Int, contextMaxTokens: Int): Int =
    maxInputTokens * charsPerToken - contextMaxTokens * 4 - templateReserveBytes - catalogueReserveBytes -
      messageReserveBytes
}
