package com.benkio.chattelegramadapter.model

import com.benkio.chatcore.model.CommandKey

enum CallbackData {
  case PreviousPage(currentPage: Int, commandKey: CommandKey) extends CallbackData
  case NextPage(currentPage: Int, commandKey: CommandKey)     extends CallbackData
  case Media(mediaFileName: String)                           extends CallbackData
  case Show(showId: String, timestamp: Option[String])        extends CallbackData
}

extension (callbackData: CallbackData) {
  def toCallbackKey: String =
    callbackData match {
      case CallbackData.PreviousPage(currentPage, commandKey) => s"previousPage-${commandKey.asString}-$currentPage"
      case CallbackData.NextPage(currentPage, commandKey)     => s"nextPage-${commandKey.asString}-$currentPage"
      case CallbackData.Media(value)                          => s"media-$value"
      case CallbackData.Show(value, timestamp)                => s"""show-$value|${timestamp.getOrElse("")}"""
    }
}

object CallbackData {
  private def parsePagination(
      callbackDataSplit: Array[String],
      build: (Int, CommandKey) => CallbackData
  ): Option[CallbackData] =
    for {
      commandKey  <- callbackDataSplit.lift(1).flatMap(CommandKey.fromString)
      currentPage <- callbackDataSplit.lift(2).flatMap(_.toIntOption)
    } yield build(currentPage, commandKey)

  private def parseShow(raw: String): Show = {
    val payload = raw.stripPrefix("show-")
    val divider = payload.lastIndexOf('|')
    if divider < 0
    then Show(payload, None)
    else {
      val showId             = payload.take(divider)
      val maybeTimestampPart = payload.drop(divider + 1)
      val timestamp          = Option.when(maybeTimestampPart.nonEmpty)(maybeTimestampPart)
      Show(showId, timestamp)
    }
  }

  def apply(callbackData: String): CallbackData = {
    val callbackDataSplit = callbackData.split("-")
    val maybeCallbackData = for {
      callbackType <- callbackDataSplit.lift(0)
      result       <- callbackType match {
        case "previousPage" =>
          parsePagination(callbackDataSplit, PreviousPage.apply)
        case "nextPage" =>
          parsePagination(callbackDataSplit, NextPage.apply)
        case "media" =>
          Some(Media(callbackDataSplit.drop(1).mkString("-")))
        case "show" =>
          Some(parseShow(callbackData))
        case _ => None
      }
    } yield result
    maybeCallbackData.getOrElse(Media(callbackData))
  }
}
