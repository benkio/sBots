package com.benkio.telegrambotinfrastructure.model

import scala.util.matching.Regex

sealed trait TextTriggerValue

object TextTriggerValue {

  def matchValue(trigger: TextTriggerValue, source: String): Boolean = trigger match {
    case RegexTextTriggerValue(v)  => v.findFirstMatchIn(source).isDefined
    case StringTextTriggerValue(v) => source contains v
  }
}

case class StringTextTriggerValue(trigger: String) extends TextTriggerValue {
  override def toString: String = trigger
}
case class RegexTextTriggerValue(trigger: Regex) extends TextTriggerValue {
  override def toString: String = trigger.toString
}

sealed trait Trigger

sealed trait MessageTrigger extends Trigger

case class TextTrigger(triggers: List[TextTriggerValue]) extends MessageTrigger
case class MessageLengthTrigger(messageLength: Int)      extends MessageTrigger
case class CommandTrigger(command: String)               extends Trigger
