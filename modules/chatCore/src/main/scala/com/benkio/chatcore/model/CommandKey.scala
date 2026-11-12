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
  case LoadCalendar  extends CommandKey("loadcalendar")
  case ShowCalendar  extends CommandKey("showcalendar")
  case TopTwenty     extends CommandKey("toptwenty")
  case SetTimeout    extends CommandKey("settimeout")
  case GetTimeout    extends CommandKey("gettimeout")

  def trigger: CommandTrigger = CommandTrigger(asString)
}

object CommandKey {
  val commandTriggers: List[CommandTrigger] = CommandKey.values.toList.map(_.trigger)

  /** The key of the command written in the input, with the command syntax of the platform (`/command` unless said
    * otherwise).
    */
  def fromString(input: String, syntax: CommandSyntax = CommandSyntax.Slash): Option[CommandKey] = {
    val normalized = syntax.commandName(input).toLowerCase(java.util.Locale.ROOT)
    CommandKey.values.find(_.asString == normalized)
  }

  def toStringValue(key: CommandKey): String = key.asString
}
