package com.benkio.telegrambotinfrastructure.model

import cats.Show
import cats.implicits._

import scala.util.matching.Regex

sealed trait TextTriggerValue

object TextTriggerValue {

  def matchValue(trigger: TextTriggerValue, source: String): Boolean = trigger match {
    case RegexTextTriggerValue(v)  => v.findFirstMatchIn(source).isDefined
    case StringTextTriggerValue(v) => source contains v
  }

  implicit val showInstance: Show[TextTriggerValue] = Show.show(ttv =>
    ttv match {
      case StringTextTriggerValue(t) => t
      case RegexTextTriggerValue(t)  => t.toString
    }
  )
}

case class StringTextTriggerValue(trigger: String) extends TextTriggerValue
case class RegexTextTriggerValue(trigger: Regex)   extends TextTriggerValue

sealed trait Trigger

sealed trait MessageTrigger extends Trigger

case class TextTrigger(triggers: TextTriggerValue*) extends MessageTrigger
case class MessageLengthTrigger(messageLength: Int) extends MessageTrigger
case object NewMemberTrigger                        extends MessageTrigger
case object LeftMemberTrigger                       extends MessageTrigger
case class CommandTrigger(command: String)          extends Trigger

object Trigger {

  implicit val showInstance: Show[Trigger] = Show.show(t =>
    t match {
      case TextTrigger(tvs @ _*)   => tvs.map(_.show).mkString("\n")
      case MessageLengthTrigger(l) => s"Trigger when the length of message exceed $l"
      case NewMemberTrigger        => "Trigger on new member joining a group"
      case LeftMemberTrigger       => "Trigger when a member leaves a group"
      case CommandTrigger(c)       => c
    }
  )

  implicit val orderingInstance: Ordering[Trigger] = new Ordering[Trigger] {
    def compare(trigger1: Trigger, trigger2: Trigger) =
      triggerLongestString(trigger1).compare(triggerLongestString(trigger2))

    private def triggerLongestString(trigger: Trigger): Int = trigger match {
      case TextTrigger(lt @ _*)      => lt.map(_.toString).max.length
      case MessageLengthTrigger(_)   => 0
      case _: NewMemberTrigger.type  => 0
      case _: LeftMemberTrigger.type => 0
      case CommandTrigger(c)         => c.length
    }
  }
}
