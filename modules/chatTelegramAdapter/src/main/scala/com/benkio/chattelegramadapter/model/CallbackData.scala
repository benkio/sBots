package com.benkio.chattelegramadapter.model

import com.benkio.chatcore.model.CommandKey

enum CallbackData {
  case PreviousPage(currentPage: Int, commandKey: CommandKey) extends CallbackData
  case NextPage(currentPage: Int, commandKey: CommandKey)     extends CallbackData
  case Media(mediaFileName: String)                           extends CallbackData
  case Show(showId: String)                                   extends CallbackData
}

extension (callbackData: CallbackData) {
  def toCallbackKey: String =
    callbackData match {
      case CallbackData.PreviousPage(currentPage, commandKey) => s"previousPage-${commandKey.asString}-$currentPage"
      case CallbackData.NextPage(currentPage, commandKey)     => s"nextPage-${commandKey.asString}-$currentPage"
      case CallbackData.Media(value)                          => s"media-$value"
      case CallbackData.Show(value)                           => s"show-$value"
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
          Some(Show(callbackDataSplit.drop(1).mkString("-")))
        case _ => None
      }
    } yield result
    maybeCallbackData.getOrElse(Media(callbackData))
  }
}
