package com.benkio.chattelegramadapter.model

import cats.syntax.all.*
import cats.Show
import com.benkio.chatcore.messagefiltering.getContent
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message

sealed trait TelegramKeyboardTitle
case class SearchCommandTelegramKeyboardTitle(value: String) extends TelegramKeyboardTitle
case class IdentityTelegramKeyboardTitle(value: String)      extends TelegramKeyboardTitle

private def extractOriginalContent(value: String): String =
  value.linesIterator
    .dropWhile(line => !line.startsWith("Input"))
    .drop(1)
    .nextOption()
    .getOrElse(value)
    .trim

private def stripCommandPrefix(input: String): String = {
  val trimmed = input.trim
  if !trimmed.startsWith("/") then trimmed
  else trimmed.drop(1).dropWhile(!_.isWhitespace).trim
}

extension (telegramKeyboardTitle: TelegramKeyboardTitle) {
  def extractInput: String = telegramKeyboardTitle match {
    case SearchCommandTelegramKeyboardTitle(value) =>
      stripCommandPrefix(extractOriginalContent(value))
    case IdentityTelegramKeyboardTitle(value) => value
  }
}

object SearchCommandTelegramKeyboardTitle {

  def build[A: Show](m: Message, input: A, valuesCount: Int): SearchCommandTelegramKeyboardTitle =
    SearchCommandTelegramKeyboardTitle(s"""Input ($valuesCount):
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
