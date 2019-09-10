package com.benkio.telegramBotInfrastructure.model

sealed trait Trigger

sealed trait MessageTrigger extends Trigger

case class TextTrigger(triggers: List[String]) extends MessageTrigger
case class MessageLengthTrigger(messageLength: Int) extends MessageTrigger
case class CommandTrigger(command: String) extends Trigger
