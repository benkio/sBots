package com.benkio.chattelegramadapter.model

import cats.syntax.all.*
import cats.Show
import com.benkio.chatcore.messagefiltering.getContent
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message

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

  def build[A: Show](m: Message, input: A): SearchCommandTelegramKeyboardTitle =
    SearchCommandTelegramKeyboardTitle(s"""Input:
                                          |${m.getContent.getOrElse("")}
                                          |
                                          |${input.show}""".stripMargin)

}

object TelegramKeyboardTitle {

  def toTelegramKeyboardTitle(msg: Message, commandKey: CommandKey): TelegramKeyboardTitle = commandKey match {
    case CommandKey.TriggerSearch | CommandKey.SearchShow =>
      SearchCommandTelegramKeyboardTitle(msg.getContent.getOrElse(""))
    case _ =>
      IdentityTelegramKeyboardTitle(msg.getContent.getOrElse(""))
  }
}
