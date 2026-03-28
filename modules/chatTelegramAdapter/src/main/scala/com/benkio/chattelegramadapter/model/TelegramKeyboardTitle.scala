package com.benkio.chattelegramadapter.model

import cats.syntax.all.*
import com.benkio.chatcore.messagefiltering.getContent
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Trigger

sealed trait TelegramKeyboardTitle
case class SearchCommandTelegramKeyboardTitle(value: String) extends TelegramKeyboardTitle
case class IdentityTelegramKeyboardTitle(value: String)      extends TelegramKeyboardTitle

extension (telegramKeyboardTitle: TelegramKeyboardTitle) {
  def extractInput: String = telegramKeyboardTitle match {
    case SearchCommandTelegramKeyboardTitle(value) =>
      value.linesIterator.drop(1).takeWhile(_.nonEmpty).mkString("\n")
    case IdentityTelegramKeyboardTitle(value) => value
  }
}

object SearchCommandTelegramKeyboardTitle {

  def build(m: Message, trigger: Trigger): SearchCommandTelegramKeyboardTitle =
    SearchCommandTelegramKeyboardTitle(s"""Input:
                                          |${m.getContent.getOrElse("")}
                                          |
                                          |${trigger.show}""".stripMargin)

}

object TelegramKeyboardTitle {

  def toTelegramKeyboardTitle(msg: Message, commandKey: CommandKey): TelegramKeyboardTitle = commandKey match {
    case CommandKey.TriggerSearch => SearchCommandTelegramKeyboardTitle(msg.getContent.getOrElse(""))
    case _                        => IdentityTelegramKeyboardTitle(msg.getContent.getOrElse(""))
  }
}
