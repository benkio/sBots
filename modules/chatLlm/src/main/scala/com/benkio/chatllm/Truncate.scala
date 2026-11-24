package com.benkio.chatllm

/** Length caps counted in UTF-16 chars, the unit `estimateTokens` and the input limit use, so a capped text can never
  * be longer than its cap in the budget's own terms. A surrogate pair is never cut in half.
  */
private[chatllm] object Truncate {
  def utf16(content: String, max: Int): String =
    if content.length <= max then content
    else {
      val keep = math.max(0, max - 1)
      val end  = if keep > 0 && Character.isHighSurrogate(content.charAt(keep - 1)) then keep - 1 else keep
      content.substring(0, end) + "…"
    }
}
