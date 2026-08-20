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
  def apply(callbackData: String): CallbackData = {
    val callbackDataSplit = callbackData.split("-")
    val maybePagination   = for {
      callbackType <- callbackDataSplit.lift(0)
      commandKey   <- callbackDataSplit.lift(1).flatMap(CommandKey.fromString)
      currentPage  <- callbackDataSplit.lift(2).flatMap(_.toIntOption)
      dataId       <- callbackDataSplit.lift(2)
      result       <- callbackType match {
        case "previousPage" => Some(PreviousPage(currentPage = currentPage, commandKey = commandKey))
        case "nextPage"     => Some(NextPage(currentPage = currentPage, commandKey = commandKey))
        case "media"        => Some(Media(dataId))
        case "show"         => Some(Show(dataId))
        case _              => None
      }
    } yield result
    maybePagination.getOrElse(Media(callbackData))
  }
}
