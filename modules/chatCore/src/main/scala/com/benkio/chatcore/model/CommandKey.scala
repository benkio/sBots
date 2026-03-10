package com.benkio.chatcore.model


/** Known, first-class command identifiers supported by `chatCore`.
  *
  * The string value is the canonical command without the leading '/' and without '@botname'.
  */
enum CommandKey(val asString: String) {
  case Random        extends CommandKey("random")
  case SearchShow    extends CommandKey("searchshow")
  case TriggerList   extends CommandKey("triggerlist")
  case TriggerSearch extends CommandKey("triggersearch")
  case Instructions  extends CommandKey("instructions")
  case Subscribe     extends CommandKey("subscribe")
  case Unsubscribe   extends CommandKey("unsubscribe")
  case Subscriptions extends CommandKey("subscriptions")
  case TopTwenty     extends CommandKey("toptwenty")
  case Timeout       extends CommandKey("timeout")

  def trigger: CommandTrigger = CommandTrigger(asString)
}

object CommandKey {
  val commandTriggers: List[CommandTrigger] = CommandKey.values.toList.map(_.trigger)

  def fromString(input: String): Option[CommandKey] = {
    val normalized = normalizeCommand(input)
    CommandKey.values.find(_.asString == normalized)
  }

  def toStringValue(key: CommandKey): String = key.asString

  private def normalizeCommand(input: String): String =
    input.trim
      .stripPrefix("/")
      .takeWhile(c => c != '@' && !c.isWhitespace)
      .toLowerCase(java.util.Locale.ROOT)

}
