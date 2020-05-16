package com.benkio.telegramBotInfrastructure.model

import scala.util.matching.Regex

sealed trait TextTriggerValue

object TextTriggerValue {

  def matchValue(trigger: TextTriggerValue, source: String): Boolean = trigger match {
    case RegexTextTriggerValue(v) => (v findFirstMatchIn source).isDefined
    case StringTextTriggerValue(v) => source contains v
  }
}

case class StringTextTriggerValue(trigger: String) extends TextTriggerValue
case class RegexTextTriggerValue(trigger: Regex) extends TextTriggerValue

sealed trait Trigger

sealed trait MessageTrigger extends Trigger

case class TextTrigger(triggers: List[TextTriggerValue]) extends MessageTrigger
case class MessageLengthTrigger(messageLength: Int) extends MessageTrigger
case class CommandTrigger(command: String) extends Trigger
