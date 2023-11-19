package com.benkio.telegrambotinfrastructure.model

import cats.Show
import cats.implicits.*

import scala.util.matching.Regex

sealed trait TextTriggerValue {
  def length: Int
}

object TextTriggerValue {

  def matchValue(trigger: TextTriggerValue, source: String): Boolean = trigger match {
    case RegexTextTriggerValue(v, _) => v.findFirstMatchIn(source).isDefined
    case StringTextTriggerValue(v)   => source contains v
  }

  given showInstance: Show[TextTriggerValue] = Show.show(ttv =>
    ttv match {
      case StringTextTriggerValue(t)   => t
      case RegexTextTriggerValue(t, _) => t.toString
    }
  )
}

case class StringTextTriggerValue(trigger: String) extends TextTriggerValue {
  override def length: Int = trigger.length
}
case class RegexTextTriggerValue(trigger: Regex, minimalLengthMatch: Int) extends TextTriggerValue {
  override def length: Int = minimalLengthMatch
}

extension (sc: StringContext) def stt(args: Any*): StringTextTriggerValue = StringTextTriggerValue(sc.s(args: _*))
extension (r: Regex)
  def tr(minimalLengthMatch: Int): RegexTextTriggerValue = RegexTextTriggerValue(r, minimalLengthMatch)

sealed trait Trigger

sealed trait MessageTrigger extends Trigger

case class TextTrigger(triggers: TextTriggerValue*) extends MessageTrigger
case class MessageLengthTrigger(messageLength: Int) extends MessageTrigger
case object NewMemberTrigger                        extends MessageTrigger
case object LeftMemberTrigger                       extends MessageTrigger
case class CommandTrigger(command: String)          extends Trigger

object Trigger {

  given showInstance: Show[Trigger] = Show.show(t =>
    t match {
      case TextTrigger(tvs @ _*)   => tvs.map(_.show).mkString("\n")
      case MessageLengthTrigger(l) => s"Trigger when the length of message exceed $l"
      case NewMemberTrigger        => "Trigger on new member joining a group"
      case LeftMemberTrigger       => "Trigger when a member leaves a group"
      case CommandTrigger(c)       => c
    }
  )

  given orderingInstance: Ordering[Trigger] = new Ordering[Trigger] {
    def compare(trigger1: Trigger, trigger2: Trigger) =
      triggerLongestString(trigger1).compare(triggerLongestString(trigger2))
  }

  def triggerLongestString(trigger: Trigger): Int = trigger match {
    case TextTrigger(lt @ _*)      => lt.map(_.length).max
    case MessageLengthTrigger(_)   => 0
    case _: NewMemberTrigger.type  => 0
    case _: LeftMemberTrigger.type => 0
    case CommandTrigger(c)         => c.length
  }
}
